# ГидПоинт

**ГидПоинт** — маркетплейс экскурсий с местными гидами. Бронирование туров, рейтинги и отзывы.

## Возможности

- 🔍 Поиск и просмотр экскурсий по категориям
- 📅 Бронирование экскурсий с выбором даты и количества участников
- ⭐ Система рейтингов и отзывов для гидов и экскурсий
- 👤 Регистрация гидов с профилями и сертификатами
- 📊 Статистика для гидов (экскурсии, бронирования, выручка, отзывы)
- 📊 Управление бронированиями: **гид** подтверждает/отклоняет/завершает брони, **клиент** может отменить свою бронь
- 💳 Оплата бронирований через **Stripe** (Checkout)
- 🔐 Аутентификация (JWT и вход через Google OAuth2)

## Технологии

### Backend
- Java 17
- Spring Boot 3.2.0
- PostgreSQL
- Redis
- Kafka
- JWT Authentication
- Google OAuth2 (вход через Google)
- Stripe (оплата бронирований)
- Flyway (миграции базы данных)

### Frontend
- React 18
- TypeScript
- Vite
- Tailwind CSS
- React Query
- Zustand

## Паттерны проектирования

### Backend (Spring Boot)

| Паттерн | Где используется |
|--------|-------------------|
| **Слоистая архитектура** | Controller → Service → Repository; разделение ответственности по пакетам. |
| **Repository (DAO)** | Интерфейсы `*Repository extends JpaRepository` — абстракция доступа к БД. |
| **Dependency Injection** | Внедрение зависимостей через конструктор (`@RequiredArgsConstructor`, `private final`). |
| **MVC** | Контроллеры обрабатывают запросы, сервисы — логику, сущности и DTO — данные. |
| **DTO** | Классы в `dto/` для запросов/ответов API без раскрытия сущностей. |
| **Adapter** | `JwtAuthenticationFilter`, `OAuth2LoginSuccessHandler`, `CustomUserDetailsService` (адаптация к Spring Security). |
| **Strategy** | Разные стратегии аутентификации (JWT / OAuth2) в конфигурации Security. |
| **Template Method** | `OncePerRequestFilter` → переопределение `doFilterInternal` в `JwtAuthenticationFilter`. |
| **Facade** | Сервисы скрывают репозитории, кэш, Kafka, уведомления за единым API. |
| **Декоратор (AOP)** | `@Cacheable` / `@CacheEvict` — кэширование ответов (tours, bookings, users и т.д.). |
| **Event-Driven** | Kafka: отправка событий (`OrderEvent`), `@KafkaListener` в `OrderEventConsumer`. |
| **Exception Handler** | `@RestControllerAdvice` в `GlobalExceptionHandler` — единая обработка ошибок. |
| **Singleton** | Сервисы и компоненты Spring — один экземпляр на контекст. |

### Frontend (React)

| Паттерн | Где используется |
|--------|-------------------|
| **Server State (React Query)** | `useQuery`, `useMutation`, `useQueryClient` — загрузка, кэш и инвалидация данных с API. |
| **Container/Presentational** | Страницы запрашивают данные и передают в компоненты; разделение логики и отображения. |

## Запуск проекта

### Используя Docker Compose

```bash
docker-compose up -d
```

Сервисы будут доступны по адресам:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Тесты (backend)

- **Unit-тесты** (JUnit 5, Mockito): сервисы `TourService`, `CategoryService`, `AuthService`, `UserService`, `BookingService`. Покрытие бизнес-логики.
- **Integration API** (MockMvc): проверка эндпоинтов `/api/tours`, `/api/categories`, `/api/auth/register` с реальной БД (Testcontainers).
- **@DataJpaTest**: тесты репозиториев `CategoryRepository`, `TourRepository` на встроенной H2.

Запуск без Docker (только unit + DataJpaTest):

```bash
cd backend && mvn test
```

Запуск всех тестов, включая интеграционные с Testcontainers (нужен Docker):

```bash
cd backend && mvn test -Pintegration
```

Для Java 23 в `pom.xml` задан аргумент `-Dnet.bytebuddy.experimental=true` для совместимости Mockito.

### Тестовые пользователи

База данных автоматически инициализируется через Flyway миграции при первом запуске.

Для первоначального входа рекомендуется входить как админ:
- **Логин:** admin
- **Пароль:** admin123

Другие тестовые пользователи:
- **Гид 1:** guide1 / guide123
- **Гид 2:** guide2 / guide123
- **Клиент 1:** customer1 / customer123
- **Клиент 2:** customer2 / customer123

### Тестовые данные

При первом запуске автоматически создаются:
- 6 категорий экскурсий (Исторические, Культурные, Пешеходные, Гастрономические, Природные, Вечерние)
- 8 тестовых экскурсий по Беларуси: Мирский замок, Несвижский замок, Национальный художественный музей, Музей истории ВОВ, прогулка по Минску, белорусская кухня, Беловежская пуща, вечерний Минск (с картинками для карточек)
- 5 тестовых пользователей (1 админ, 2 гида, 2 клиента)

Все данные можно использовать для тестирования функционала маркетплейса.

### Ошибка Flyway «Migration checksum mismatch»

Если после изменения миграций появляется ошибка проверки контрольной суммы:

1. Убедись, что контейнер с PostgreSQL запущен (`docker compose up -d postgres`).
2. В папке `backend` выполни:
   ```bash
   mvn flyway:repair
   ```
3. Перезапусти backend (`docker compose up -d --build backend` или перезапуск приложения).

Либо обнови сумму вручную в БД (подставь нужные `version` и `checksum` из сообщения «Resolved locally»):
   ```bash
   docker compose exec postgres psql -U postgres -d marketplace_db -c "UPDATE flyway_schema_history SET checksum = 2036254564 WHERE version = '5';"
   ```

### Kafka падает после перезагрузки ноутбука

Для Kafka **отключён постоянный том** — при каждом `docker compose up` Kafka стартует с чистого состояния, поэтому после перезагрузки ПК контейнер должен подниматься без ошибок (InconsistentClusterIdException и т.п.).

Если у тебя остался старый том `marketplace_kafka_data` и Kafka по-прежнему падает, один раз выполни:
```bash
docker compose down
docker volume rm marketplace_kafka_data
docker compose up -d
```

**Ошибка Kafka при старте: `NodeExists` (KeeperErrorCode = NodeExists)**  
Она появляется, когда в Zookeeper уже есть узел регистрации брокера (например, предыдущий контейнер Kafka не завершился корректно). Сделай полный перезапуск стека, чтобы Zookeeper и Kafka поднялись заново с чистым состоянием:
```bash
docker compose down
docker compose up -d
```
Подожди 1–2 минуты: сначала должен стать healthy Zookeeper, затем Kafka. Если ошибка повторится, проверь, не запущен ли ещё один Kafka/Zookeeper вне Docker (например, локальная установка) — останови их или смени порты в `docker-compose.yml`.

Backend не ждёт успешного старта Kafka (приложение может работать без него), поэтому даже при падении Kafka поднимутся frontend и backend.

### Ошибка backend «Unable to start web server»

Если при старте backend падает с *«Unable to start web server»*, смотри в логах блок **«Caused by»** — там указана настоящая причина. Частые варианты:

- **Порт 8080 занят** — закрой другое приложение на 8080 или запусти backend с другим портом: `server.port=8081`.
- **Postgres/Redis/Kafka недоступны** — при запуске через Docker убедись, что все сервисы подняты и healthy:  
  `docker compose ps`  
  При необходимости перезапусти: `docker compose up -d` и подожди 1–2 минуты, затем перезапусти backend:  
  `docker compose up -d --build backend`.

Чтобы увидеть отчёт по условиям автоконфигурации, можно перезапустить backend с debug:  
`docker compose run --rm -e SPRING_PROFILES_ACTIVE=debug backend` (если в образе есть профиль с `debug=true`) или добавить в команду контейнера аргумент `--debug`.

### На «Все экскурсии» показываются заглушки вместо фото

Список «все экскурсии» кэшируется в Redis. При старте приложения кэш туров очищается, поэтому после перезапуска backend фото должны подтянуться из БД. Если заглушки остались, перезапусти backend:  
`docker compose up -d --build backend`.

### Google OAuth2

Для входа через Google:

1. Создай проект в [Google Cloud Console](https://console.cloud.google.com/).
2. Включи **Google+ API** (или **Google Identity**).
3. Создай учётные данные **OAuth 2.0 Client ID** (тип: Web application).
4. **Важно (ошибка redirect_uri_mismatch):** в **Authorized redirect URIs** добавь **ровно** такой URI, какой уходит в Google из приложения (без слэша в конце, без лишних портов):
   - Заходишь на сайт по **http://localhost:8080** (backend напрямую) → добавь:  
     `http://localhost:8080/api/login/oauth2/code/google`
   - Заходишь по **http://localhost:3000** (Docker/nginx) → добавь:  
     `http://localhost:3000/api/login/oauth2/code/google`
   - В production: `https://твой-домен/api/login/oauth2/code/google`  
   В приложении этот же URI задаётся в `spring.security.oauth2.client.registration.google.redirect-uri` или переменной `GOOGLE_OAUTH_REDIRECT_URI`.
5. Скопируй Client ID и Client Secret.
6. Задай переменные окружения (или в `application.properties`):
   ```bash
   GOOGLE_OAUTH_CLIENT_ID=твой-client-id
   GOOGLE_OAUTH_CLIENT_SECRET=твой-client-secret
   # если используешь вход через localhost:3000 (Docker):
   GOOGLE_OAUTH_REDIRECT_URI=http://localhost:3000/api/login/oauth2/code/google
   ```
7. Убедись, что `app.oauth2.frontend-redirect-uri` указывает на страницу callback фронта:  
   - Локально (Vite): `http://localhost:3001/auth/callback`  
   - Docker: `http://localhost:3000/auth/callback`  
   - Production: `https://твой-домен/auth/callback`

### Stripe (оплата)

Оплата бронирований идёт через Stripe Checkout. После создания брони пользователь перенаправляется на страницу оплаты Stripe; после успешной оплаты вебхук помечает бронирование как оплаченное.

1. Зарегистрируйся на [Stripe](https://stripe.com) и получи ключи в **Developers → API keys** (Secret key).
2. Задай переменные окружения (или в `application.properties`):
   ```bash
   STRIPE_SECRET_KEY=sk_test_...
   STRIPE_WEBHOOK_SECRET=whsec_...   # для вебхука (см. ниже)
   APP_FRONTEND_URL=http://localhost:3001   # или http://localhost:3000 при Docker
   ```
3. **Вебхук** (чтобы помечать брони как оплаченные после оплаты):
   - В Stripe Dashboard: **Developers → Webhooks → Add endpoint**.
   - URL: `https://твой-бэкенд/api/payments/webhook` (локально можно использовать [Stripe CLI](https://stripe.com/docs/stripe-cli) для проброса: `stripe listen --forward-to localhost:8080/api/payments/webhook`).
   - Событие: `checkout.session.completed`.
   - Скопируй **Signing secret** и задай его в `STRIPE_WEBHOOK_SECRET`.

Цены на сайте в BYN. Stripe не поддерживает BYN, поэтому списание идёт в EUR по курсу (`stripe.byn-to-eur-rate`, по умолчанию 0.28). В описании платежа на Stripe указывается сумма в BYN.

## API Endpoints

Основные эндпоинты:
- `/api/tours` - управление экскурсиями
- `/api/bookings` - управление бронированиями
- `/api/reviews` - отзывы и рейтинги
- `/api/payments` - создание сессии Stripe Checkout, вебхук
- `/api/categories` - категории экскурсий
- `/api/auth` - аутентификация

## Производительность

**Цель:** время отклика API &lt; 200 мс для 95% запросов.

- **БД:** запросы оптимизированы через `@EntityGraph` и `JOIN FETCH` (туры — category, guide; бронирования — customer, tour, guide), составные индексы в миграции `V11__Performance_indexes.sql` (tours: active+category, guide+active).
- **Профилирование:** при необходимости включи в `application.properties`:  
  `spring.jpa.show-sql=true` и `spring.jpa.properties.hibernate.format_sql=true` — для анализа запросов; для детального профиля БД — профиль с `spring.datasource.hikari.leak-detection-threshold` и мониторинг через Actuator.
- **Метрики:** включён endpoint `/actuator/metrics` и гистограмма по времени ответа HTTP (`management.metrics.distribution.percentiles-histogram.http.server.requests=true`).  
  Проверка p95:
  1. Запусти backend и дай нагрузку (ручные запросы или нагрузочный тест, например через Apache Bench или k6).
  2. Открой: `GET /actuator/metrics/http.server.requests` — в ответе смотри статистику по тегам (uri, method). Для перцентилей запроси: `GET /actuator/metrics/http.server.requests?tag=percentile:0.95` (или используй готовый дашборд Grafana/Prometheus при наличии).
- **Нагрузочное тестирование:** для формальной проверки «95% запросов &lt; 200 мс» можно использовать, например, `k6` или `ab` по основным GET-эндпоинтам (`/api/tours`, `/api/categories`, `/api/tours/{id}`) и затем сверить с метриками Actuator.