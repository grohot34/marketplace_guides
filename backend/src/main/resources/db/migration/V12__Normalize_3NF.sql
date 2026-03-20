-- Приведение к третьей нормальной форме: удаление избыточных атрибутов.
-- guide_id в bookings выводится из tour_id -> tours.guide_id.
-- customer_id, guide_id, tour_id в reviews выводится из booking_id -> bookings.
-- average_rating, total_ratings в users и tours вычисляются по отзывам.

-- 1. Bookings: убрать guide_id (гид определяется через tour)
ALTER TABLE bookings DROP CONSTRAINT IF EXISTS fk_booking_guide;
ALTER TABLE bookings DROP COLUMN IF EXISTS guide_id;

-- 2. Reviews: убрать customer_id, guide_id, tour_id (определяются через booking)
ALTER TABLE reviews DROP CONSTRAINT IF EXISTS fk_review_customer;
ALTER TABLE reviews DROP CONSTRAINT IF EXISTS fk_review_guide;
ALTER TABLE reviews DROP CONSTRAINT IF EXISTS fk_review_tour;
ALTER TABLE reviews DROP COLUMN IF EXISTS customer_id;
ALTER TABLE reviews DROP COLUMN IF EXISTS guide_id;
ALTER TABLE reviews DROP COLUMN IF EXISTS tour_id;

-- 3. Users: убрать хранимые рейтинги (вычисляются по отзывам)
ALTER TABLE users DROP COLUMN IF EXISTS average_rating;
ALTER TABLE users DROP COLUMN IF EXISTS total_ratings;

-- 4. Tours: убрать хранимые рейтинги (вычисляются по отзывам)
ALTER TABLE tours DROP COLUMN IF EXISTS average_rating;
ALTER TABLE tours DROP COLUMN IF EXISTS total_ratings;
