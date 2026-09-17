CREATE TABLE place_osm_metadata
(
    place_id BIGINT PRIMARY KEY,

    osm_type VARCHAR(16) NOT NULL,
    osm_id BIGINT NOT NULL,

    tags JSONB NOT NULL DEFAULT '{}'::jsonb,

    CONSTRAINT fk_place_osm_metadata_place
        FOREIGN KEY (place_id)
            REFERENCES place (id)
            ON DELETE CASCADE,

    CONSTRAINT ck_place_osm_metadata_type
        CHECK (osm_type IN ('NODE', 'WAY', 'RELATION')),

    CONSTRAINT uq_place_osm_metadata_object
        UNIQUE (osm_type, osm_id)
);

CREATE UNIQUE INDEX uq_place_source_external_id
    ON place (source_type, external_id)
    WHERE external_id IS NOT NULL;