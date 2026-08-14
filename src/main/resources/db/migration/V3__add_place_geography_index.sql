CREATE INDEX idx_place_location_geography
    ON place
    USING GIST ((location::geography));