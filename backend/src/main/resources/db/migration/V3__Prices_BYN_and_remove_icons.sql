-- Convert tour prices from RUB to BYN (approximate: divide by ~30)
-- and remove category icons
UPDATE tours SET price_per_person = ROUND(price_per_person / 30.0, 2) WHERE price_per_person > 100;
UPDATE categories SET icon = NULL;
