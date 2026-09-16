CREATE TABLE place_score
(
    place_id BIGINT PRIMARY KEY,

    nature          NUMERIC(4, 3) NOT NULL DEFAULT 0,
    attractions     NUMERIC(4, 3) NOT NULL DEFAULT 0,
    military        NUMERIC(4, 3) NOT NULL DEFAULT 0,
    religion        NUMERIC(4, 3) NOT NULL DEFAULT 0,
    architecture    NUMERIC(4, 3) NOT NULL DEFAULT 0,
    history         NUMERIC(4, 3) NOT NULL DEFAULT 0,
    art             NUMERIC(4, 3) NOT NULL DEFAULT 0,
    souvenirs       NUMERIC(4, 3) NOT NULL DEFAULT 0,
    transport_tech  NUMERIC(4, 3) NOT NULL DEFAULT 0,
    accommodation   NUMERIC(4, 3) NOT NULL DEFAULT 0,
    food            NUMERIC(4, 3) NOT NULL DEFAULT 0,
    exclusive_food  NUMERIC(4, 3) NOT NULL DEFAULT 0,
    subcultures     NUMERIC(4, 3) NOT NULL DEFAULT 0,

    CONSTRAINT fk_place_score_place
        FOREIGN KEY (place_id)
            REFERENCES place (id)
            ON DELETE CASCADE,

    CONSTRAINT ck_place_score_nature
        CHECK (nature BETWEEN 0 AND 1),

    CONSTRAINT ck_place_score_attractions
        CHECK (attractions BETWEEN 0 AND 1),

    CONSTRAINT ck_place_score_military
        CHECK (military BETWEEN 0 AND 1),

    CONSTRAINT ck_place_score_religion
        CHECK (religion BETWEEN 0 AND 1),

    CONSTRAINT ck_place_score_architecture
        CHECK (architecture BETWEEN 0 AND 1),

    CONSTRAINT ck_place_score_history
        CHECK (history BETWEEN 0 AND 1),

    CONSTRAINT ck_place_score_art
        CHECK (art BETWEEN 0 AND 1),

    CONSTRAINT ck_place_score_souvenirs
        CHECK (souvenirs BETWEEN 0 AND 1),

    CONSTRAINT ck_place_score_transport_tech
        CHECK (transport_tech BETWEEN 0 AND 1),

    CONSTRAINT ck_place_score_accommodation
        CHECK (accommodation BETWEEN 0 AND 1),

    CONSTRAINT ck_place_score_food
        CHECK (food BETWEEN 0 AND 1),

    CONSTRAINT ck_place_score_exclusive_food
        CHECK (exclusive_food BETWEEN 0 AND 1),

    CONSTRAINT ck_place_score_subcultures
        CHECK (subcultures BETWEEN 0 AND 1)
);


-- Для объектов, которые уже существуют в БД.
INSERT INTO place_score (place_id)
SELECT id
FROM place
    ON CONFLICT (place_id) DO NOTHING;