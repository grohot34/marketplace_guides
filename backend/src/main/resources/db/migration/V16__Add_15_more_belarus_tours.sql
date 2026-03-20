-- 15 дополнительных экскурсий по Беларуси

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Брестская крепость',
    'Мемориальный комплекс «Брестская крепость-герой»: история обороны 1941 года, музей, монументы и Вечный огонь.',
    40.00, 3, 25,
    'Брест, пр. Машерова',
    'Главный вход в мемориал',
    '1. Холмские ворота' || E'\n' || '2. Музей обороны' || E'\n' || '3. Скульптура «Мужество»' || E'\n' || '4. Вечный огонь',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide1'), TRUE
FROM categories c WHERE c.name = 'Исторические'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Брестская крепость');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Лидский замок',
    'Экскурсия по замку Гедимина в Лиде: единственный в Беларуси замок-кастель, рыцарские турниры и средневековые программы.',
    38.00, 2, 20,
    'Гродненская область, г. Лида',
    'Вход в замковый двор',
    '1. Внешние стены и башни' || E'\n' || '2. Внутренний двор' || E'\n' || '3. Экспозиция и анимация',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide1'), TRUE
FROM categories c WHERE c.name = 'Исторические'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Лидский замок');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Гродно: Старый и Новый замки',
    'Пешеходная экскурсия по историческому центру Гродно: Старый замок, Новый замок, Коложская церковь, Фарный костёл.',
    42.00, 3, 15,
    'Гродно, ул. Замковая',
    'Площадь перед Старым замком',
    '1. Старый замок' || E'\n' || '2. Новый замок' || E'\n' || '3. Коложская церковь' || E'\n' || '4. Советская улица',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide1'), TRUE
FROM categories c WHERE c.name = 'Исторические'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Гродно: Старый и Новый замки');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Дворец Пусловских в Коссово',
    'Неоготический дворец XIX века и усадьба Тадеуша Костюшко в Коссово. Восстановление и история рода Пусловских.',
    35.00, 2, 12,
    'Брестская область, г. Коссово',
    'Парковка у дворца',
    '1. Внешний осмотр дворца' || E'\n' || '2. Экспозиция' || E'\n' || '3. Дом-музей Костюшко',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide2'), TRUE
FROM categories c WHERE c.name = 'Исторические'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Дворец Пусловских в Коссово');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Ружанский дворец Сапегов',
    'Руины величественного дворца магнатов Сапегов в Ружанах. История «белорусского Версаля» и музей.',
    32.00, 2, 15,
    'Брестская область, п. Ружаны',
    'Вход на территорию дворца',
    '1. Въездная брама' || E'\n' || '2. Руины дворца' || E'\n' || '3. Музейная экспозиция',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide1'), TRUE
FROM categories c WHERE c.name = 'Исторические'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Ружанский дворец Сапегов');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Национальная библиотека Беларуси',
    'Экскурсия по знаменитому зданию-ромбокубооктаэдру: обзорные площадки, музей книги, панорама Минска.',
    28.00, 2, 20,
    'Минск, пр. Независимости, 116',
    'Главный вход в библиотеку',
    '1. Обзорная площадка' || E'\n' || '2. Музей книги' || E'\n' || '3. Экспозиция и читальные залы',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide2'), TRUE
FROM categories c WHERE c.name = 'Культурные'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Национальная библиотека Беларуси');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Большой театр оперы и балета',
    'Экскурсия за кулисы главного театра страны: история здания, репетиционные залы, костюмерные и сцена.',
    30.00, 2, 15,
    'Минск, пл. Парижской Коммуны, 1',
    'Парадный вход в театр',
    '1. Зрительный зал' || E'\n' || '2. Закулисье' || E'\n' || '3. Музей театра',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide2'), TRUE
FROM categories c WHERE c.name = 'Культурные'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Большой театр оперы и балета');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Дудутки: музей старинных ремёсел',
    'Этнографический комплекс под Минском: гончарная, кузница, хлебопекарня, сыроварня, старинные автомобили и угощение.',
    48.00, 4, 18,
    'Минская область, Пуховичский район',
    'Вход в музейный комплекс',
    '1. Ремесленные мастерские' || E'\n' || '2. Дегустация' || E'\n' || '3. Конюшня и техника' || E'\n' || '4. Сувениры',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide2'), TRUE
FROM categories c WHERE c.name = 'Культурные'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Дудутки: музей старинных ремёсел');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Троицкое предместье и Остров слёз',
    'Пешая прогулка по старинному кварталу на берегу Свислочи и мемориалу воинам-афганцам.',
    25.00, 2, 15,
    'Минск, Троицкое предместье',
    'У моста через Свислочь',
    '1. Улочки Троицкого' || E'\n' || '2. Остров слёз' || E'\n' || '3. Набережная',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide1'), TRUE
FROM categories c WHERE c.name = 'Пешеходные'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Троицкое предместье и Остров слёз');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Проспект Независимости: сталинский ампир',
    'Архитектурная экскурсия по главной оси Минска: от площади Ленина до площади Независимости, история застройки.',
    28.00, 2, 20,
    'Минск, пр. Независимости',
    'Площадь Ленина, у ГУМа',
    '1. Площадь Ленина' || E'\n' || '2. Окружной дом офицеров' || E'\n' || '3. Главпочтамт' || E'\n' || '4. Площадь Независимости',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide1'), TRUE
FROM categories c WHERE c.name = 'Пешеходные'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Проспект Независимости: сталинский ампир');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Дегустация белорусского сыра и мёда',
    'Выезд на агроусадьбу: производство сыра, пасека, дегустация мёда и сыров с травяным чаем.',
    45.00, 3, 10,
    'Минская область, агроусадьба',
    'Сбор у автобуса (место уточняется)',
    '1. Сыроварня' || E'\n' || '2. Пасека' || E'\n' || '3. Дегустация',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide2'), TRUE
FROM categories c WHERE c.name = 'Гастрономические'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Дегустация белорусского сыра и мёда');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Озеро Нарочь',
    'Однодневная поездка на крупнейшее озеро Беларуси: пляжи, прогулка на катере, природа и история курорта.',
    55.00, 6, 12,
    'Минская область, Мядельский район',
    'Парковка у Нарочи',
    '1. Обзор озера' || E'\n' || '2. Прогулка на катере' || E'\n' || '3. Свободное время у воды',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide1'), TRUE
FROM categories c WHERE c.name = 'Природные'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Озеро Нарочь');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Браславские озёра',
    'Национальный парк «Браславские озёра»: экотропы, смотровые площадки, рыбалка и отдых на природе.',
    50.00, 5, 15,
    'Витебская область, г. Браслав',
    'Визит-центр национального парка',
    '1. Обзорная по парку' || E'\n' || '2. Экотропа' || E'\n' || '3. Озёра и пляжи',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide1'), TRUE
FROM categories c WHERE c.name = 'Природные'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Браславские озёра');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Голубые озёра',
    'Пеший маршрут по заказнику «Голубые озёра»: карстовые озёра, сосновые леса и живописные тропы.',
    42.00, 4, 12,
    'Витебская область, Мядельский район',
    'Вход в заказник',
    '1. Тропа к озёрам' || E'\n' || '2. Озёра Глубля и Глубелька' || E'\n' || '3. Смотровые точки',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide1'), TRUE
FROM categories c WHERE c.name = 'Природные'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Голубые озёра');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Минск с высоты: смотровые площадки',
    'Вечерняя экскурсия с подъёмом на смотровые площадки Национальной библиотеки и панорамой ночного города.',
    35.00, 2, 15,
    'Минск, Национальная библиотека',
    'Вход в библиотеку',
    '1. Подъём на обзорную площадку' || E'\n' || '2. Панорама вечернего Минска' || E'\n' || '3. Фотосессия',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide2'), TRUE
FROM categories c WHERE c.name = 'Вечерние'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Минск с высоты: смотровые площадки');

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active)
SELECT
    'Ночной Брест: город у границы',
    'Вечерняя прогулка по центру Бреста: пешеходная улица Советская, набережная, огни города и история.',
    32.00, 2, 15,
    'Брест, ул. Советская',
    'Площадь Ленина',
    '1. Советская улица' || E'\n' || '2. Набережная Мухавца' || E'\n' || '3. Подсвеченные памятники',
    NULL, c.id, (SELECT id FROM users WHERE username = 'guide1'), TRUE
FROM categories c WHERE c.name = 'Вечерние'
AND NOT EXISTS (SELECT 1 FROM tours t WHERE t.title = 'Ночной Брест: город у границы');
