package com.example.backend.config;

import com.example.backend.model.Category;
import com.example.backend.model.Tour;
import com.example.backend.model.User;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.TourRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DataLoader {

    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile("!prod") 
    public CommandLineRunner initDatabase(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            TourRepository tourRepository) {
        return args -> {
            log.info("DataLoader started");
        
            // Flyway миграции уже загрузили данные, поэтому просто проверяем и обновляем пароли если нужно
            try {
                long userCount = userRepository.count();
                log.info("Current user count in database: {}", userCount);
                
                if (userCount > 0) {
                    log.info("Database already initialized by Flyway migrations");
                    
                    // Обновляем пароли пользователей правильными хешами если они были созданы с placeholder хешами
                    updateUserPasswordIfNeeded(userRepository, "admin", "admin123");
                    updateUserPasswordIfNeeded(userRepository, "guide1", "guide123");
                    updateUserPasswordIfNeeded(userRepository, "guide2", "guide123");
                    updateUserPasswordIfNeeded(userRepository, "customer1", "customer123");
                    updateUserPasswordIfNeeded(userRepository, "customer2", "customer123");
                    
                    log.info("Password updates completed (if needed)");
                    return;
                }
                
                log.warn("No users found in database. Flyway migrations should have loaded test data.");
                log.warn("If you see this message, check Flyway migration status.");
            } catch (Exception e) {
                log.error("Error checking database state: ", e);
            }

            try {
                log.info("Initializing database with test data...");

                User admin = createUser(
                    "admin",
                    "admin@example.com",
                    "admin123",
                    "Администратор",
                    "Системы",
                    "+7 (999) 000-00-01",
                    User.Role.ADMIN
            );

                User guide1 = createUser(
                        "guide1",
                        "guide1@example.com",
                        "guide123",
                        "Иван",
                        "Иванов",
                        "+7 (999) 111-11-11",
                        User.Role.GUIDE
                );
                guide1.setBio("Опытный гид с 10-летним стажем. Провожу экскурсии по историческим местам Москвы.");
                guide1.setLanguages("Русский, Английский");
                guide1.setCertifications("Лицензия гида-экскурсовода");

                User guide2 = createUser(
                        "guide2",
                        "guide2@example.com",
                        "guide123",
                        "Мария",
                        "Петрова",
                        "+7 (999) 222-22-22",
                        User.Role.GUIDE
                );
                guide2.setBio("Профессиональный гид по искусству и культуре. Специализируюсь на музеях и галереях.");
                guide2.setLanguages("Русский, Английский, Французский");
                guide2.setCertifications("Сертификат экскурсовода по музеям");

                User customer1 = createUser(
                        "customer1",
                        "customer1@example.com",
                        "customer123",
                        "Алексей",
                        "Сидоров",
                        "+7 (999) 333-33-33",
                        User.Role.CUSTOMER
                );

                User customer2 = createUser(
                        "customer2",
                        "customer2@example.com",
                        "customer123",
                        "Елена",
                        "Козлова",
                        "+7 (999) 444-44-44",
                        User.Role.CUSTOMER
                );

                List<User> users = userRepository.saveAll(List.of(admin, guide1, guide2, customer1, customer2));
                log.info("Created {} users", users.size());


                Category historical = createCategory(
                        "Исторические",
                        "Экскурсии по историческим местам и памятникам",
                         "🏛️"
                );

                Category cultural = createCategory(
                        "Культурные",
                        "Экскурсии по музеям, галереям и культурным объектам",
                              "🎨"
                );

                Category walking = createCategory(
                        "Пешеходные",
                        "Пешеходные экскурсии по городу",
                              "🚶"
                );

                Category food = createCategory(
                        "Гастрономические",
                        "Экскурсии с дегустацией местной кухни",
                             "🍽️"
                );

                Category nature = createCategory(
                        "Природные",
                        "Экскурсии на природе и за городом",
                        "🌲"
                );

                Category night = createCategory(
                        "Вечерние",
                        "Вечерние и ночные экскурсии",
                           "🌙"
                );

                List<Category> categories = categoryRepository.saveAll(
                        List.of(historical, cultural, walking, food, nature, night)
                );
                log.info("Created {} categories", categories.size());

    
                List<Tour> tours = List.of(
                
                    createTour(
                            "Красная площадь и Кремль",
                            "Пешеходная экскурсия по главной площади Москвы с посещением Кремля. Узнайте историю создания Красной площади и архитектурных памятников.",
                            new BigDecimal("2500"),
                            3,
                            15,
                            "Москва, Красная площадь",
                            "Встреча у памятника Минину и Пожарскому",
                            "1. Красная площадь\n2. Собор Василия Блаженного\n3. Мавзолей Ленина\n4. ГУМ\n5. Кремль (внешний осмотр)",
                            historical,
                            guide1,
                            "https://images.unsplash.com/photo-1513326738677-b964603b136d?w=800"
                    ),
                    createTour(
                            "Тайны старой Москвы",
                            "Прогулка по историческим улочкам Замоскворечья и Заяузья. Откройте для себя скрытые уголки столицы.",
                            new BigDecimal("2000"),
                            2,
                            12,
                            "Москва, Замоскворечье",
                            "Станция метро Новокузнецкая",
                            "1. Улица Большая Ордынка\n2. Храм Христа Спасителя\n3. Патриарший мост\n4. Старые улочки",
                            historical,
                            guide1,
                            null
                    ),
                    createTour(
                            "Третьяковская галерея",
                            "Экскурсия по знаменитой галерее с рассказом о русском искусстве. Посещение основных залов и шедевров.",
                            new BigDecimal("3000"),
                            2,
                            10,
                            "Москва, Лаврушинский переулок",
                            "Вход в Третьяковскую галерею",
                            "1. Древнерусское искусство\n2. Искусство XVIII-XIX веков\n3. Шедевры русских художников",
                            cultural,
                            guide2,
                            "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800"
                    ),
                    createTour(
                            "Эрмитаж и Зимний дворец",
                            "Виртуальная экскурсия по главному музею Санкт-Петербурга с рассказом об истории и коллекциях.",
                            new BigDecimal("2500"),
                            2,
                            20,
                            "Санкт-Петербург, Дворцовая площадь",
                            "Главный вход в Эрмитаж",
                            "1. Зимний дворец\n2. Залы Эрмитажа\n3. Шедевры мировой живописи",
                            cultural,
                            guide2,
                            null
                    ),
                    createTour(
                            "Московские бульвары",
                            "Пешеходная экскурсия по знаменитым московским бульварам: Тверской, Никитский, Гоголевский.",
                            new BigDecimal("1500"),
                            2,
                            20,
                            "Москва, Тверской бульвар",
                            "Станция метро Пушкинская",
                            "1. Тверской бульвар\n2. Никитский бульвар\n3. Гоголевский бульвар\n4. Памятники и истории",
                            walking,
                            guide1,
                            null
                    ),
                    createTour(
                            "Московская кухня",
                            "Гастрономическая экскурсия с дегустацией традиционных блюд московской кухни в лучших ресторанах.",
                            new BigDecimal("4000"),
                            3,
                            8,
                            "Москва, центр",
                            "Ресторан 'У Ильича'",
                            "1. Дегустация блюд\n2. История московской кухни\n3. Посещение 3 ресторанов",
                            food,
                            guide2,
                            "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800"
                    ),
                    createTour(
                            "Сокольники и парк",
                            "Экскурсия по парку Сокольники с рассказом об истории и природе. Прогулка по аллеям и прудам.",
                            new BigDecimal("1800"),
                            2,
                            15,
                            "Москва, парк Сокольники",
                            "Главный вход в парк",
                            "1. История парка\n2. Прогулка по аллеям\n3. Пруды и природа",
                            nature,
                            guide1,
                            null
                    ),
                    createTour(
                            "Ночная Москва",
                            "Вечерняя экскурсия по освещенной Москве. Посещение смотровых площадок и красивых мест.",
                            new BigDecimal("3000"),
                            3,
                            12,
                            "Москва, центр",
                            "Станция метро Охотный ряд",
                            "1. Красная площадь вечером\n2. Смотровая площадка Воробьевых гор\n3. Москва-Сити",
                            night,
                            guide2,
                            "https://images.unsplash.com/photo-1513807779085-89ba5b8d8f5d?w=800"
                    )
                );

                List<Tour> savedTours = tourRepository.saveAll(tours);
                log.info("Created {} tours", savedTours.size());

                log.info("Database initialization completed successfully!");
                log.info("Test users:");
                log.info("  Admin: username=admin, password=admin123");
                log.info("  Guide 1: username=guide1, password=guide123");
                log.info("  Guide 2: username=guide2, password=guide123");
                log.info("  Customer 1: username=customer1, password=customer123");
                log.info("  Customer 2: username=customer2, password=customer123");
            } catch (Exception e) {
                log.error("Error during database initialization: ", e);
                throw e;
            }
        };
    }

    private User createUser(String username, String email, String password,
                           String firstName, String lastName, String phone, User.Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private Category createCategory(String name, String description, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setIcon(icon);
        return category;
    }

    private Tour createTour(String title, String description, BigDecimal pricePerPerson,
                           Integer durationHours, Integer maxParticipants, String location,
                           String meetingPoint, String itinerary, Category category, User guide, String imageUrl) {
        Tour tour = new Tour();
        tour.setTitle(title);
        tour.setDescription(description);
        tour.setPricePerPerson(pricePerPerson);
        tour.setDurationHours(durationHours);
        tour.setMaxParticipants(maxParticipants);
        tour.setLocation(location);
        tour.setMeetingPoint(meetingPoint);
        tour.setItinerary(itinerary);
        tour.setCategory(category);
        tour.setGuide(guide);
        tour.setImageUrl(imageUrl);
        tour.setActive(true);
        tour.setAverageRating(0.0);
        tour.setTotalRatings(0);
        return tour;
    }

    private void updateUserPasswordIfNeeded(UserRepository userRepository, String username, String password) {
        try {
            userRepository.findByUsername(username).ifPresent(user -> {
                // Проверяем, нужно ли обновить пароль (если он не соответствует правильному формату)
                String currentHash = user.getPassword();
                // Если хеш слишком короткий или не начинается с $2a$, обновляем его
                if (currentHash == null || currentHash.length() < 60 || !currentHash.startsWith("$2a$")) {
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user);
                    log.info("Updated password for user: {}", username);
                }
            });
        } catch (Exception e) {
            log.warn("Could not update password for user {}: {}", username, e.getMessage());
        }
    }
}

