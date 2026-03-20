-- Замена российских номеров (+7) на белорусские (+375) у тестовых пользователей
UPDATE users SET phone = '+375 (29) 000-00-01' WHERE username = 'admin' AND phone LIKE '+7%';
UPDATE users SET phone = '+375 (29) 111-11-11' WHERE username = 'guide1' AND phone LIKE '+7%';
UPDATE users SET phone = '+375 (29) 222-22-22' WHERE username = 'guide2' AND phone LIKE '+7%';
UPDATE users SET phone = '+375 (29) 333-33-33' WHERE username = 'customer1' AND phone LIKE '+7%';
UPDATE users SET phone = '+375 (29) 444-44-44' WHERE username = 'customer2' AND phone LIKE '+7%';
