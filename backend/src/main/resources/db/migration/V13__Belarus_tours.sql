-- Замена российских экскурсий на белорусские (известные места Беларуси) и привязка картинок

UPDATE tours SET
    title = 'Мирский замок',
    description = 'Экскурсия по одному из символов Беларуси — замку в Мире. История рода Радзивиллов, архитектура эпохи Ренессанса и Готики, музей и парк.',
    price_per_person = 45.00,
    duration_hours = 3,
    max_participants = 15,
    location = 'Гродненская область, п. Мир',
    meeting_point = 'Парковка у стен замка',
    itinerary = '1. Внешний осмотр замка' || E'\n' || '2. Внутренние залы и экспозиция' || E'\n' || '3. Часовня-усыпальница' || E'\n' || '4. Парк и итальянский сад',
    image_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/da/Mir_Castle_complex_2014.jpg/800px-Mir_Castle_complex_2014.jpg'
WHERE title = 'Красная площадь и Кремль';

UPDATE tours SET
    title = 'Несвижский замок и парк',
    description = 'Дворцово-парковый ансамбль в Несвиже — бывшая резиденция Радзивиллов. Залы дворца, легенды о Чёрной даме, старинный парк.',
    price_per_person = 50.00,
    duration_hours = 4,
    max_participants = 12,
    location = 'Минская область, г. Несвиж',
    meeting_point = 'Вход в замковый комплекс',
    itinerary = '1. Парадные залы дворца' || E'\n' || '2. Оружейная палата' || E'\n' || '3. Парк и пруды' || E'\n' || '4. Костёл Божьего Тела',
    image_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Niasvi%C5%BE_Castle_2014.jpg/800px-Niasvi%C5%BE_Castle_2014.jpg'
WHERE title = 'Тайны старой Москвы';

UPDATE tours SET
    title = 'Национальный художественный музей',
    description = 'Крупнейшее в Беларуси собрание искусства: иконопись, живопись XVIII–XX веков, белорусский авангард и современное искусство.',
    price_per_person = 35.00,
    duration_hours = 2,
    max_participants = 10,
    location = 'Минск, ул. Ленина, 20',
    meeting_point = 'Главный вход в музей',
    itinerary = '1. Древнебелорусское искусство' || E'\n' || '2. Русское и европейское искусство' || E'\n' || '3. Белорусское искусство XX века',
    image_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/5d/National_Art_Museum_of_Belarus_2014.jpg/800px-National_Art_Museum_of_Belarus_2014.jpg'
WHERE title = 'Третьяковская галерея';

UPDATE tours SET
    title = 'Музей истории Великой Отечественной войны',
    description = 'Мемориальный комплекс и музей в Минске: история войны, залы памяти, экспозиция военной техники под открытым небом.',
    price_per_person = 40.00,
    duration_hours = 3,
    max_participants = 20,
    location = 'Минск, пр. Победителей, 8',
    meeting_point = 'Площадь перед музеем',
    itinerary = '1. Залы довоенного периода' || E'\n' || '2. Оккупация и сопротивление' || E'\n' || '3. Зал Победы' || E'\n' || '4. Внешняя экспозиция',
    image_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Belarusian_Great_Patriotic_War_Museum_2022.jpg/800px-Belarusian_Great_Patriotic_War_Museum_2022.jpg'
WHERE title = 'Эрмитаж и Зимний дворец';

UPDATE tours SET
    title = 'Прогулка по центру Минска',
    description = 'Пешеходная экскурсия по проспекту Независимости, Верхнему городу, Троицкому предместью. Архитектура сталинского ампира и старый Минск.',
    price_per_person = 30.00,
    duration_hours = 2,
    max_participants = 20,
    location = 'Минск, пр. Независимости',
    meeting_point = 'Площадь Независимости, у ГУМа',
    itinerary = '1. Проспект Независимости' || E'\n' || '2. Верхний город и Ратуша' || E'\n' || '3. Троицкое предместье' || E'\n' || '4. Остров слёз',
    image_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/85/Minsk_Independence_Square_2014.jpg/800px-Minsk_Independence_Square_2014.jpg'
WHERE title = 'Московские бульвары';

UPDATE tours SET
    title = 'Белорусская кухня: драники и крамбамбуля',
    description = 'Гастрономическая экскурсия с дегустацией традиционных блюд: драники, колдуны, мачанка, крамбамбуля и домашние наливки.',
    price_per_person = 55.00,
    duration_hours = 3,
    max_participants = 8,
    location = 'Минск, ресторан «Камяніца»',
    meeting_point = 'Вход в ресторан',
    itinerary = '1. История белорусской кухни' || E'\n' || '2. Дегустация горячих блюд' || E'\n' || '3. Десерты и напитки',
    image_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/Draniki.jpg/800px-Draniki.jpg'
WHERE title = 'Московская кухня';

UPDATE tours SET
    title = 'Беловежская пуща',
    description = 'Экскурсия по древнему заповедному лесу — объект ЮНЕСКО. Вольеры с зубрами и оленями, музей природы, резиденция Деда Мороза зимой.',
    price_per_person = 60.00,
    duration_hours = 5,
    max_participants = 15,
    location = 'Брестская область, Каменецкий район',
    meeting_point = 'Вход в национальный парк',
    itinerary = '1. Музей природы' || E'\n' || '2. Вольеры с зубрами' || E'\n' || '3. Прогулка по экотропе' || E'\n' || '4. Поместье Деда Мороза (по сезону)',
    image_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Bia%C5%82owie%C5%BCa_Forest_%282%29.jpg/800px-Bia%C5%82owie%C5%BCa_Forest_%282%29.jpg'
WHERE title = 'Сокольники и парк';

UPDATE tours SET
    title = 'Вечерний Минск',
    description = 'Вечерняя экскурсия по подсвеченному центру Минска: проспект Независимости, Октябрьская площадь, набережная Свислочи.',
    price_per_person = 35.00,
    duration_hours = 2,
    max_participants = 12,
    location = 'Минск, центр',
    meeting_point = 'Площадь Октябрьская',
    itinerary = '1. Октябрьская площадь' || E'\n' || '2. Проспект Независимости вечером' || E'\n' || '3. Набережная и панорама',
    image_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/9e/Minsk_at_night_2014.jpg/800px-Minsk_at_night_2014.jpg'
WHERE title = 'Ночная Москва';
