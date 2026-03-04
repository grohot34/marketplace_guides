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
                    "+375 (29) 000-00-01",
                    User.Role.ADMIN
            );

                User guide1 = createUser(
                        "guide1",
                        "guide1@example.com",
                        "guide123",
                        "Иван",
                        "Иванов",
                        "+375 (29) 111-11-11",
                        User.Role.GUIDE
                );
                guide1.setBio("Опытный гид с 10-летним стажем. Провожу экскурсии по замкам и историческим местам Беларуси.");
                guide1.setLanguages("Русский, Белорусский, Английский");
                guide1.setCertifications("Лицензия гида-экскурсовода");

                User guide2 = createUser(
                        "guide2",
                        "guide2@example.com",
                        "guide123",
                        "Мария",
                        "Петрова",
                        "+375 (29) 222-22-22",
                        User.Role.GUIDE
                );
                guide2.setBio("Профессиональный гид по искусству и культуре. Специализируюсь на музеях Минска и гастрономических турах.");
                guide2.setLanguages("Русский, Белорусский, Английский");
                guide2.setCertifications("Сертификат экскурсовода по музеям");

                User customer1 = createUser(
                        "customer1",
                        "customer1@example.com",
                        "customer123",
                        "Алексей",
                        "Сидоров",
                        "+375 (29) 333-33-33",
                        User.Role.CUSTOMER
                );

                User customer2 = createUser(
                        "customer2",
                        "customer2@example.com",
                        "customer123",
                        "Елена",
                        "Козлова",
                        "+375 (29) 444-44-44",
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
                            "Мирский замок",
                            "Экскурсия по одному из символов Беларуси — замку в Мире. История рода Радзивиллов, архитектура эпохи Ренессанса и Готики.",
                            new BigDecimal("45"),
                            3,
                            15,
                            "Гродненская область, п. Мир",
                            "Парковка у стен замка",
                            "1. Внешний осмотр замка\n2. Внутренние залы и экспозиция\n3. Часовня-усыпальница\n4. Парк и итальянский сад",
                            historical,
                            guide1,
                            "/images/tours/мирский замок.jpg"
                    ),
                    createTour(
                            "Несвижский замок и парк",
                            "Дворцово-парковый ансамбль в Несвиже — бывшая резиденция Радзивиллов. Залы дворца, легенды о Чёрной даме, старинный парк.",
                            new BigDecimal("50"),
                            4,
                            12,
                            "Минская область, г. Несвиж",
                            "Вход в замковый комплекс",
                            "1. Парадные залы дворца\n2. Оружейная палата\n3. Парк и пруды\n4. Костёл Божьего Тела",
                            historical,
                            guide1,
                            "/images/tours/несвижский замок.webp"
                    ),
                    createTour(
                            "Национальный художественный музей",
                            "Крупнейшее в Беларуси собрание искусства: иконопись, живопись XVIII–XX веков, белорусский авангард и современное искусство.",
                            new BigDecimal("35"),
                            2,
                            10,
                            "Минск, ул. Ленина, 20",
                            "Главный вход в музей",
                            "1. Древнебелорусское искусство\n2. Русское и европейское искусство\n3. Белорусское искусство XX века",
                            cultural,
                            guide2,
                            "/images/tours/национальный художественный музей.jpg"
                    ),
                    createTour(
                            "Музей истории Великой Отечественной войны",
                            "Мемориальный комплекс и музей в Минске: история войны, залы памяти, экспозиция военной техники под открытым небом.",
                            new BigDecimal("40"),
                            3,
                            20,
                            "Минск, пр. Победителей, 8",
                            "Площадь перед музеем",
                            "1. Залы довоенного периода\n2. Оккупация и сопротивление\n3. Зал Победы\n4. Внешняя экспозиция",
                            cultural,
                            guide2,
                            "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Belarusian_Great_Patriotic_War_Museum_2022.jpg/800px-Belarusian_Great_Patriotic_War_Museum_2022.jpg"
                    ),
                    createTour(
                            "Прогулка по центру Минска",
                            "Пешеходная экскурсия по проспекту Независимости, Верхнему городу, Троицкому предместью. Архитектура сталинского ампира и старый Минск.",
                            new BigDecimal("30"),
                            2,
                            20,
                            "Минск, пр. Независимости",
                            "Площадь Независимости, у ГУМа",
                            "1. Проспект Независимости\n2. Верхний город и Ратуша\n3. Троицкое предместье\n4. Остров слёз",
                            walking,
                            guide1,
                            "/images/tours/прогулка по центру минска.webp"
                    ),
                    createTour(
                            "Белорусская кухня: драники",
                            "Гастрономическая экскурсия с дегустацией традиционных блюд: драники, колдуны, мачанка и домашние наливки.",
                            new BigDecimal("55"),
                            3,
                            8,
                            "Минск, ресторан «Камяніца»",
                            "Вход в ресторан",
                            "1. История белорусской кухни\n2. Дегустация горячих блюд\n3. Десерты и напитки",
                            food,
                            guide2,
                            "/images/tours/драники.jpg"
                    ),
                    createTour(
                            "Беловежская пуща",
                            "Экскурсия по древнему заповедному лесу — объект ЮНЕСКО. Вольеры с зубрами и оленями, музей природы, резиденция Деда Мороза зимой.",
                            new BigDecimal("60"),
                            5,
                            15,
                            "Брестская область, Каменецкий район",
                            "Вход в национальный парк",
                            "1. Музей природы\n2. Вольеры с зубрами\n3. Прогулка по экотропе\n4. Поместье Деда Мороза (по сезону)",
                            nature,
                            guide1,
                            "/images/tours/беловежская пуща.jpg"
                    ),
                    createTour(
                            "Вечерний Минск",
                            "Вечерняя экскурсия по подсвеченному центру Минска: проспект Независимости, Октябрьская площадь, набережная Свислочи.",
                            new BigDecimal("35"),
                            2,
                            12,
                            "Минск, центр",
                            "Площадь Октябрьская",
                            "1. Октябрьская площадь\n2. Проспект Независимости вечером\n3. Набережная и панорама",
                            night,
                            guide2,
                            "/images/tours/ночная прогулка.jfif"
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

