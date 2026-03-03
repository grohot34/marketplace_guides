-- Сопоставление картинок с экскурсиями (файлы в frontend/public/images/tours/)
-- Пробелы в именах файлов заменены на %20 для корректной загрузки в браузере

UPDATE tours SET image_url = '/images/tours/red-square.jpg' WHERE title = 'Красная площадь и Кремль';
UPDATE tours SET image_url = '/images/tours/замоскворечье.jpg' WHERE title = 'Тайны старой Москвы';
UPDATE tours SET image_url = '/images/tours/третьяковская%20галерея.jpg' WHERE title = 'Третьяковская галерея';
UPDATE tours SET image_url = '/images/tours/эрмитаж.jpg' WHERE title = 'Эрмитаж и Зимний дворец';
UPDATE tours SET image_url = '/images/tours/тверской%20бульвар.jpg' WHERE title = 'Московские бульвары';
UPDATE tours SET image_url = '/images/tours/московская-кухня.webp' WHERE title = 'Московская кухня';
UPDATE tours SET image_url = '/images/tours/сокольники.jpg' WHERE title = 'Сокольники и парк';
UPDATE tours SET image_url = '/images/tours/ночная%20москва.jpg' WHERE title = 'Ночная Москва';
