-- Insert test categories (no icons)
INSERT INTO categories (name, description, icon) VALUES
('Исторические', 'Экскурсии по историческим местам и памятникам', NULL),
('Культурные', 'Экскурсии по музеям, галереям и культурным объектам', NULL),
('Пешеходные', 'Пешеходные экскурсии по городу', NULL),
('Гастрономические', 'Экскурсии с дегустацией местной кухни', NULL),
('Природные', 'Экскурсии на природе и за городом', NULL),
('Вечерние', 'Вечерние и ночные экскурсии', NULL)
ON CONFLICT (name) DO NOTHING;

-- Insert test users (passwords are bcrypt hashed: admin123, guide123, customer123)
-- Using Spring Boot BCryptPasswordEncoder standard hashes (strength 10)
-- Note: DataLoader will update passwords with proper hashes if needed
-- For testing, using placeholder hash - DataLoader ensures correct hashes are set
INSERT INTO users (username, email, password, first_name, last_name, phone, role, active, bio, languages, certifications) VALUES
('admin', 'admin@example.com', '$2a$10$placeholder_hash_will_be_updated_by_dataloader', 'Администратор', 'Системы', '+7 (999) 000-00-01', 'ADMIN', TRUE, NULL, NULL, NULL),
('guide1', 'guide1@example.com', '$2a$10$placeholder_hash_will_be_updated_by_dataloader', 'Иван', 'Иванов', '+7 (999) 111-11-11', 'GUIDE', TRUE, 'Опытный гид с 10-летним стажем. Провожу экскурсии по историческим местам Москвы.', 'Русский, Английский', 'Лицензия гида-экскурсовода'),
('guide2', 'guide2@example.com', '$2a$10$placeholder_hash_will_be_updated_by_dataloader', 'Мария', 'Петрова', '+7 (999) 222-22-22', 'GUIDE', TRUE, 'Профессиональный гид по искусству и культуре. Специализируюсь на музеях и галереях.', 'Русский, Английский, Французский', 'Сертификат экскурсовода по музеям'),
('customer1', 'customer1@example.com', '$2a$10$placeholder_hash_will_be_updated_by_dataloader', 'Алексей', 'Сидоров', '+7 (999) 333-33-33', 'CUSTOMER', TRUE, NULL, NULL, NULL),
('customer2', 'customer2@example.com', '$2a$10$placeholder_hash_will_be_updated_by_dataloader', 'Елена', 'Козлова', '+7 (999) 444-44-44', 'CUSTOMER', TRUE, NULL, NULL, NULL)
ON CONFLICT (username) DO NOTHING;

-- Insert test tours
INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active, average_rating, total_ratings)
SELECT 
    'Красная площадь и Кремль',
    'Пешеходная экскурсия по главной площади Москвы с посещением Кремля. Узнайте историю создания Красной площади и архитектурных памятников.',
    85.00,
    3,
    15,
    'Москва, Красная площадь',
    'Встреча у памятника Минину и Пожарскому',
    '1. Красная площадь' || E'\n' || '2. Собор Василия Блаженного' || E'\n' || '3. Мавзолей Ленина' || E'\n' || '4. ГУМ' || E'\n' || '5. Кремль (внешний осмотр)',
    NULL,
    c.id,
    (SELECT id FROM users WHERE username = 'guide1'),
    TRUE,
    0.0,
    0
FROM categories c WHERE c.name = 'Исторические'
ON CONFLICT DO NOTHING;

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active, average_rating, total_ratings)
SELECT 
    'Тайны старой Москвы',
    'Прогулка по историческим улочкам Замоскворечья и Заяузья. Откройте для себя скрытые уголки столицы.',
    70.00,
    2,
    12,
    'Москва, Замоскворечье',
    'Станция метро Новокузнецкая',
    '1. Улица Большая Ордынка' || E'\n' || '2. Храм Христа Спасителя' || E'\n' || '3. Патриарший мост' || E'\n' || '4. Старые улочки',
    NULL,
    c.id,
    (SELECT id FROM users WHERE username = 'guide1'),
    TRUE,
    0.0,
    0
FROM categories c WHERE c.name = 'Исторические'
ON CONFLICT DO NOTHING;

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active, average_rating, total_ratings)
SELECT 
    'Третьяковская галерея',
    'Экскурсия по знаменитой галерее с рассказом о русском искусстве. Посещение основных залов и шедевров.',
    100.00,
    2,
    10,
    'Москва, Лаврушинский переулок',
    'Вход в Третьяковскую галерею',
    '1. Древнерусское искусство' || E'\n' || '2. Искусство XVIII-XIX веков' || E'\n' || '3. Шедевры русских художников',
    NULL,
    c.id,
    (SELECT id FROM users WHERE username = 'guide2'),
    TRUE,
    0.0,
    0
FROM categories c WHERE c.name = 'Культурные'
ON CONFLICT DO NOTHING;

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active, average_rating, total_ratings)
SELECT 
    'Эрмитаж и Зимний дворец',
    'Виртуальная экскурсия по главному музею Санкт-Петербурга с рассказом об истории и коллекциях.',
    85.00,
    2,
    20,
    'Санкт-Петербург, Дворцовая площадь',
    'Главный вход в Эрмитаж',
    '1. Зимний дворец' || E'\n' || '2. Залы Эрмитажа' || E'\n' || '3. Шедевры мировой живописи',
    NULL,
    c.id,
    (SELECT id FROM users WHERE username = 'guide2'),
    TRUE,
    0.0,
    0
FROM categories c WHERE c.name = 'Культурные'
ON CONFLICT DO NOTHING;

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active, average_rating, total_ratings)
SELECT 
    'Московские бульвары',
    'Пешеходная экскурсия по знаменитым московским бульварам: Тверской, Никитский, Гоголевский.',
    50.00,
    2,
    20,
    'Москва, Тверской бульвар',
    'Станция метро Пушкинская',
    '1. Тверской бульвар' || E'\n' || '2. Никитский бульвар' || E'\n' || '3. Гоголевский бульвар' || E'\n' || '4. Памятники и истории',
    NULL,
    c.id,
    (SELECT id FROM users WHERE username = 'guide1'),
    TRUE,
    0.0,
    0
FROM categories c WHERE c.name = 'Пешеходные'
ON CONFLICT DO NOTHING;

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active, average_rating, total_ratings)
SELECT 
    'Московская кухня',
    'Гастрономическая экскурсия с дегустацией традиционных блюд московской кухни в лучших ресторанах.',
    135.00,
    3,
    8,
    'Москва, центр',
    'Ресторан ''У Ильича''',
    '1. Дегустация блюд' || E'\n' || '2. История московской кухни' || E'\n' || '3. Посещение 3 ресторанов',
    NULL,
    c.id,
    (SELECT id FROM users WHERE username = 'guide2'),
    TRUE,
    0.0,
    0
FROM categories c WHERE c.name = 'Гастрономические'
ON CONFLICT DO NOTHING;

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active, average_rating, total_ratings)
SELECT 
    'Сокольники и парк',
    'Экскурсия по парку Сокольники с рассказом об истории и природе. Прогулка по аллеям и прудам.',
    60.00,
    2,
    15,
    'Москва, парк Сокольники',
    'Главный вход в парк',
    '1. История парка' || E'\n' || '2. Прогулка по аллеям' || E'\n' || '3. Пруды и природа',
    NULL,
    c.id,
    (SELECT id FROM users WHERE username = 'guide1'),
    TRUE,
    0.0,
    0
FROM categories c WHERE c.name = 'Природные'
ON CONFLICT DO NOTHING;

INSERT INTO tours (title, description, price_per_person, duration_hours, max_participants, location, meeting_point, itinerary, image_url, category_id, guide_id, active, average_rating, total_ratings)
SELECT 
    'Ночная Москва',
    'Вечерняя экскурсия по освещенной Москве. Посещение смотровых площадок и красивых мест.',
    100.00,
    3,
    12,
    'Москва, центр',
    'Станция метро Охотный ряд',
    '1. Красная площадь вечером' || E'\n' || '2. Смотровая площадка Воробьевых гор' || E'\n' || '3. Москва-Сити',
    NULL,
    c.id,
    (SELECT id FROM users WHERE username = 'guide2'),
    TRUE,
    0.0,
    0
FROM categories c WHERE c.name = 'Вечерние'
ON CONFLICT DO NOTHING;
