package ru.putevodika.place.service;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.place.dto.OsmPlaceImportRequest;
import ru.putevodika.place.dto.PlaceResponse;
import ru.putevodika.place.dto.PlaceScoresRequest;
import ru.putevodika.place.entity.*;
import ru.putevodika.place.exception.UnknownPlaceCategoryException;
import ru.putevodika.place.repository.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceImportService {

    private final PlaceRepository placeRepository;

    private final CategoryRepository categoryRepository;

    private final PlaceScoreRepository placeScoreRepository;

    private final PlaceOsmMetadataRepository
            osmMetadataRepository;

    private final GeometryFactory geometryFactory;

    private final PlaceService placeService;


    @Transactional
    public PlaceResponse importOne(
            OsmPlaceImportRequest request
    ) {
        OsmElementType osmType =
                parseOsmType(
                        request.getOsm().getType()
                );

        String externalId =
                buildExternalId(
                        request.getOsm().getType(),
                        request.getOsm().getId()
                );

        validateExternalId(
                request.getId(),
                externalId
        );

        Set<Category> categories =
                categoryRepository
                        .findAllByCodeInAndActiveTrue(
                                request.getCategories()
                        );

        validateCategories(
                request.getCategories(),
                categories
        );

        Point location =
                geometryFactory.createPoint(
                        new Coordinate(
                                request.getLocation().getLon(),
                                request.getLocation().getLat()
                        )
                );

        PlaceOsmMetadata existingMetadata =
                osmMetadataRepository
                        .findByOsmTypeAndOsmId(
                                osmType,
                                request.getOsm().getId()
                        )
                        .orElse(null);

        Place place;

        if (existingMetadata != null) {
            place = existingMetadata.getPlace();
        } else {
            place =
                    placeRepository
                            .findBySourceTypeAndExternalId(
                                    PlaceSourceType.OSM,
                                    externalId
                            )
                            .orElse(null);
        }

        if (place == null) {
            place = new Place(
                    request.getName(),
                    resolveDescription(
                            request.getDescription()
                    ),
                    null,
                    location,
                    PlaceSourceType.OSM,
                    externalId
            );
        } else {
            place.update(
                    request.getName(),
                    resolveDescription(
                            request.getDescription()
                    ),
                    place.getAddress(),
                    location
            );
        }

        place.replaceCategories(categories);

        place.updateVisitInfo(
                place.getVisitDurationMinutes(),
                request.getOpeningHours()
        );

        place.updateRouteAvailability(
                request.isAvailableForRoute()
        );

        updateContacts(
                place,
                request.getContacts()
        );

        Place saved =
                placeRepository.save(place);

        replaceScores(
                saved,
                request.getScores()
        );

        replaceOsmMetadata(
                saved,
                osmType,
                request,
                existingMetadata
        );

        return placeService.getById(
                saved.getId()
        );
    }


    private OsmElementType parseOsmType(
            String type
    ) {
        return OsmElementType.valueOf(
                type.toUpperCase(
                        Locale.ROOT
                )
        );
    }


    private String buildExternalId(
            String type,
            Long osmId
    ) {
        return "osm:"
                + type.toLowerCase(Locale.ROOT)
                + ":"
                + osmId;
    }


    private void validateExternalId(
            String requestedId,
            String expectedId
    ) {
        if (!expectedId.equals(requestedId)) {
            throw new IllegalArgumentException(
                    "Некорректный id OSM-объекта. "
                            + "Ожидается: "
                            + expectedId
            );
        }
    }


    private String resolveDescription(
            OsmPlaceImportRequest.DescriptionData description
    ) {
        if (description == null) {
            return null;
        }

        if (description.getFull() != null
                && !description.getFull().isBlank()) {

            return description.getFull();
        }

        return description.getShortText();
    }


    private void updateContacts(
            Place place,
            OsmPlaceImportRequest.ContactsData contacts
    ) {
        if (contacts == null) {
            place.updateContacts(
                    null,
                    null,
                    null
            );

            return;
        }

        place.updateContacts(
                contacts.getPhone(),
                contacts.getWebsite(),
                contacts.getEmail()
        );
    }


    private void replaceScores(
            Place place,
            PlaceScoresRequest request
    ) {
        PlaceScore scores =
                placeScoreRepository
                        .findById(place.getId())
                        .orElseGet(
                                () -> new PlaceScore(place)
                        );

        scores.update(
                request.getNature(),
                request.getAttractions(),
                request.getMilitary(),
                request.getReligion(),
                request.getArchitecture(),
                request.getHistory(),
                request.getArt(),
                request.getSouvenirs(),
                request.getTransportTech(),
                request.getAccommodation(),
                request.getFood(),
                request.getExclusiveFood(),
                request.getSubcultures()
        );

        placeScoreRepository.save(scores);
    }


    private void replaceOsmMetadata(
            Place place,
            OsmElementType osmType,
            OsmPlaceImportRequest request,
            PlaceOsmMetadata existingMetadata
    ) {
        PlaceOsmMetadata metadata =
                existingMetadata != null
                        ? existingMetadata
                        : new PlaceOsmMetadata(
                        place,
                        osmType,
                        request.getOsm().getId(),
                        request.getOsm().getTags()
                );

        metadata.updateTags(
                request.getOsm().getTags()
        );

        metadata.updateImportMetadata(
                request.getLocation()
                        .getGeometryType(),
                request.getLocation()
                        .getPointSource(),
                request.getWarnings()
        );

        osmMetadataRepository.save(metadata);
    }


    private void validateCategories(
            Set<String> requestedCodes,
            Set<Category> categories
    ) {
        Set<String> existingCodes =
                categories.stream()
                        .map(Category::getCode)
                        .collect(
                                Collectors.toSet()
                        );

        Set<String> unknownCodes =
                new HashSet<>(
                        requestedCodes
                );

        unknownCodes.removeAll(
                existingCodes
        );

        if (!unknownCodes.isEmpty()) {
            throw new UnknownPlaceCategoryException(
                    unknownCodes
            );
        }
    }
}