-- Локальные картинки для карточек экскурсий и правка тура «Белорусская кухня» (только драники)

UPDATE tours SET
    title = 'Белорусская кухня: драники',
    description = 'Гастрономическая экскурсия с дегустацией традиционных блюд: драники, колдуны, мачанка и домашние наливки.',
    image_url = '/images/tours/драники.jpg'
WHERE title = 'Белорусская кухня: драники и крамбамбуля';

UPDATE tours SET image_url = '/images/tours/мирский замок.jpg' WHERE title = 'Мирский замок';
UPDATE tours SET image_url = '/images/tours/несвижский замок.webp' WHERE title = 'Несвижский замок и парк';
UPDATE tours SET image_url = '/images/tours/национальный художественный музей.jpg' WHERE title = 'Национальный художественный музей';
UPDATE tours SET image_url = '/images/tours/прогулка по центру минска.webp' WHERE title = 'Прогулка по центру Минска';
UPDATE tours SET image_url = '/images/tours/беловежская пуща.jpg' WHERE title = 'Беловежская пуща';
UPDATE tours SET image_url = '/images/tours/ночная прогулка.jfif' WHERE title = 'Вечерний Минск';
