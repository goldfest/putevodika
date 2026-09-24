-- Сохраняем существующие веса пользователей,
-- но приводим feature.code к ключам из place_score / JSON scores.

UPDATE feature
SET code = 'military',
    name = 'Военная тематика'
WHERE code = 'MILITARY';

UPDATE feature
SET code = 'religion',
    name = 'Религиозная тематика'
WHERE code = 'RELIGIOUS';

UPDATE feature
SET code = 'architecture',
    name = 'Архитектура'
WHERE code = 'ARCHITECTURE';

UPDATE feature
SET code = 'transport_tech',
    name = 'Транспорт и техника'
WHERE code = 'TRANSPORT';


-- INTERACTIVE отсутствует в финальном scores.
DELETE FROM user_feature_preference
WHERE feature_id IN (
    SELECT id
    FROM feature
    WHERE code = 'INTERACTIVE'
);

DELETE FROM place_feature
WHERE feature_id IN (
    SELECT id
    FROM feature
    WHERE code = 'INTERACTIVE'
);

DELETE FROM feature
WHERE code = 'INTERACTIVE';


INSERT INTO feature (code, name)
VALUES
    ('nature', 'Природа'),
    ('attractions', 'Достопримечательности'),
    ('history', 'История'),
    ('art', 'Искусство'),
    ('souvenirs', 'Сувениры'),
    ('accommodation', 'Размещение'),
    ('food', 'Еда'),
    ('exclusive_food', 'Уникальная еда'),
    ('subcultures', 'Субкультуры')
    ON CONFLICT (code) DO NOTHING;