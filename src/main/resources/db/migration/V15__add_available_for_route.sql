ALTER TABLE place
    ADD COLUMN available_for_route BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_place_available_for_route
    ON place (available_for_route);