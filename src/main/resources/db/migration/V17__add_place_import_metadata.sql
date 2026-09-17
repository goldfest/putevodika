ALTER TABLE place
    ADD COLUMN phone VARCHAR(64),
    ADD COLUMN website VARCHAR(2048),
    ADD COLUMN email VARCHAR(320);

ALTER TABLE place_osm_metadata
    ADD COLUMN geometry_type VARCHAR(32),
    ADD COLUMN point_source VARCHAR(64),
    ADD COLUMN warnings JSONB NOT NULL DEFAULT '[]'::jsonb;