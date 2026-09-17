package ru.putevodika.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.putevodika.place.entity.OsmElementType;
import ru.putevodika.place.entity.PlaceOsmMetadata;

import java.util.Optional;

public interface PlaceOsmMetadataRepository
        extends JpaRepository<PlaceOsmMetadata, Long> {

    Optional<PlaceOsmMetadata>
    findByOsmTypeAndOsmId(
            OsmElementType osmType,
            Long osmId
    );
}