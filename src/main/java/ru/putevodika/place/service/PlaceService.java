package ru.putevodika.place.service;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.place.dto.CreatePlaceRequest;
import ru.putevodika.place.dto.PlaceResponse;
import ru.putevodika.place.entity.Category;
import ru.putevodika.place.entity.Place;
import ru.putevodika.place.entity.PlaceSourceType;
import ru.putevodika.place.repository.CategoryRepository;
import ru.putevodika.place.repository.PlaceRepository;
import ru.putevodika.place.dto.NearbyPlaceResponse;
import ru.putevodika.place.dto.MapPlaceResponse;
import ru.putevodika.place.exception.InvalidMapBoundsException;
import ru.putevodika.place.dto.UpdatePlaceRequest;
import ru.putevodika.feature.entity.Feature;
import ru.putevodika.feature.entity.PlaceFeature;
import ru.putevodika.feature.repository.PlaceFeatureRepository;
import ru.putevodika.feature.service.FeatureService;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import ru.putevodika.place.exception.PlaceNotFoundException;
import ru.putevodika.place.exception.UnknownPlaceCategoryException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import ru.putevodika.common.dto.PageResponse;
import ru.putevodika.place.dto.PlaceListItemResponse;
import ru.putevodika.place.entity.PlaceSourceType;

import static ru.putevodika.place.repository.specification.PlaceSpecifications.hasActive;
import static ru.putevodika.place.repository.specification.PlaceSpecifications.hasCategory;
import static ru.putevodika.place.repository.specification.PlaceSpecifications.hasSourceType;
import static ru.putevodika.place.repository.specification.PlaceSpecifications.nameContains;

import ru.putevodika.place.entity.PlaceScore;
import ru.putevodika.place.repository.PlaceScoreRepository;
import ru.putevodika.place.dto.PlaceScoresRequest;
import ru.putevodika.place.dto.PlaceScoresResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private static final double EARTH_RADIUS_METERS =
            6_371_008.8;

    private final PlaceRepository placeRepository;

    private final CategoryRepository categoryRepository;

    private final GeometryFactory geometryFactory;

    private final PlaceFeatureRepository
            placeFeatureRepository;

    private final FeatureService featureService;

    private final PlaceScoreRepository placeScoreRepository;


    @Transactional
    public PlaceResponse create(CreatePlaceRequest request) {

        Set<Category> categories =
                categoryRepository.findAllByCodeInAndActiveTrue(
                        request.getCategories()
                );

        validateCategories(
                request.getCategories(),
                categories
        );

        Map<String, Feature> features =
                featureService.resolveActive(
                        request.getFeatures().keySet()
                );

        Point location = geometryFactory.createPoint(
                new Coordinate(
                        request.getLongitude(),
                        request.getLatitude()
                )
        );

        Place place = new Place(
                request.getName(),
                request.getDescription(),
                request.getAddress(),
                location,
                PlaceSourceType.MANUAL,
                null
        );

        place.updateVisitInfo(
                request.getVisitDurationMinutes(),
                request.getOpeningHours()
        );

        categories.forEach(place::addCategory);

        Place saved = placeRepository.save(place);
        replaceFeatures(
                saved,
                features,
                request.getFeatures()
        );

        replaceScores(
                saved,
                request.getScores()
        );

        return toResponse(saved);
    }


    public PlaceResponse getById(Long id) {

        Place place = placeRepository.findById(id)
                .orElseThrow(
                        () -> new PlaceNotFoundException(id)
                );

        return toResponse(place);
    }


    private PlaceResponse toResponse(Place place) {

        Set<String> categories =
                place.getCategories()
                        .stream()
                        .map(Category::getCode)
                        .collect(Collectors.toSet());

        Map<String, Integer> features =
                placeFeatureRepository
                        .findAllByPlaceId(
                                place.getId()
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        placeFeature ->
                                                placeFeature
                                                        .getFeature()
                                                        .getCode(),

                                        placeFeature ->
                                                (int) placeFeature
                                                        .getValue()
                                )
                        );

        return PlaceResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .description(place.getDescription())
                .address(place.getAddress())
                .latitude(place.getLocation().getY())
                .longitude(place.getLocation().getX())
                .categories(categories)
                .sourceType(place.getSourceType().name())
                .active(place.isActive())
                .createdAt(place.getCreatedAt())
                .updatedAt(place.getUpdatedAt())
                .visitDurationMinutes(
                        place.getVisitDurationMinutes()
                )
                .openingHours(
                        place.getOpeningHours()
                )
                .features(features)
                .scores(
                        toScoresResponse(place.getId())
                )
                .build();
    }

    public List<NearbyPlaceResponse> findNearby(
            double latitude,
            double longitude,
            int radiusMeters,
            Set<String> categoryCodes,
            int limit
    ) {
        if (categoryCodes == null || categoryCodes.isEmpty()) {
            return placeRepository.findActiveNearby(
                            latitude,
                            longitude,
                            radiusMeters,
                            limit
                    )
                    .stream()
                    .map(place -> toNearbyResponse(
                            place,
                            latitude,
                            longitude
                    ))
                    .toList();
        }

        Set<Category> categories =
                categoryRepository.findAllByCodeInAndActiveTrue(
                        categoryCodes
                );

        validateCategories(
                categoryCodes,
                categories
        );

        return placeRepository.findActiveNearbyByCategories(
                        latitude,
                        longitude,
                        radiusMeters,
                        categoryCodes,
                        limit
                )
                .stream()
                .map(place -> toNearbyResponse(
                        place,
                        latitude,
                        longitude
                ))
                .toList();
    }

    private void validateCategories(
            Set<String> requestedCodes,
            Set<Category> categories
    ) {
        Set<String> existingCodes = categories.stream()
                .map(Category::getCode)
                .collect(Collectors.toSet());

        Set<String> unknownCodes =
                new HashSet<>(requestedCodes);

        unknownCodes.removeAll(existingCodes);

        if (!unknownCodes.isEmpty()) {
            throw new UnknownPlaceCategoryException(
                    unknownCodes
            );
        }
    }

    private NearbyPlaceResponse toNearbyResponse(
            Place place,
            double originLatitude,
            double originLongitude
    ) {
        Set<String> categories =
                place.getCategories()
                        .stream()
                        .map(Category::getCode)
                        .collect(Collectors.toSet());

        long distanceMeters = calculateDistanceMeters(
                originLatitude,
                originLongitude,
                place.getLocation().getY(),
                place.getLocation().getX()
        );

        return NearbyPlaceResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .description(place.getDescription())
                .address(place.getAddress())
                .latitude(place.getLocation().getY())
                .longitude(place.getLocation().getX())
                .categories(categories)
                .sourceType(place.getSourceType().name())
                .distanceMeters(distanceMeters)
                .build();
    }

    private long calculateDistanceMeters(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2
    ) {
        double latitudeDelta =
                Math.toRadians(latitude2 - latitude1);

        double longitudeDelta =
                Math.toRadians(longitude2 - longitude1);

        double latitude1Radians =
                Math.toRadians(latitude1);

        double latitude2Radians =
                Math.toRadians(latitude2);

        double a =
                Math.sin(latitudeDelta / 2)
                        * Math.sin(latitudeDelta / 2)
                        +
                        Math.cos(latitude1Radians)
                                * Math.cos(latitude2Radians)
                                * Math.sin(longitudeDelta / 2)
                                * Math.sin(longitudeDelta / 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        return Math.round(
                EARTH_RADIUS_METERS * c
        );
    }

    public List<MapPlaceResponse> findInBounds(
            double minLatitude,
            double minLongitude,
            double maxLatitude,
            double maxLongitude,
            Set<String> categoryCodes,
            int limit
    ) {
        validateBounds(
                minLatitude,
                minLongitude,
                maxLatitude,
                maxLongitude
        );

        List<Place> places;

        if (categoryCodes == null || categoryCodes.isEmpty()) {
            places = placeRepository.findActiveInBounds(
                    minLatitude,
                    minLongitude,
                    maxLatitude,
                    maxLongitude,
                    limit
            );
        } else {
            Set<Category> categories =
                    categoryRepository
                            .findAllByCodeInAndActiveTrue(
                                    categoryCodes
                            );

            validateCategories(
                    categoryCodes,
                    categories
            );

            places =
                    placeRepository.findActiveInBoundsByCategories(
                            minLatitude,
                            minLongitude,
                            maxLatitude,
                            maxLongitude,
                            categoryCodes,
                            limit
                    );
        }

        return places.stream()
                .map(this::toMapResponse)
                .toList();
    }

    private void validateBounds(
            double minLatitude,
            double minLongitude,
            double maxLatitude,
            double maxLongitude
    ) {
        if (minLatitude >= maxLatitude
                || minLongitude >= maxLongitude) {

            throw new InvalidMapBoundsException();
        }
    }

    private MapPlaceResponse toMapResponse(Place place) {
        Set<String> categories =
                place.getCategories()
                        .stream()
                        .map(Category::getCode)
                        .collect(Collectors.toSet());

        return MapPlaceResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .latitude(place.getLocation().getY())
                .longitude(place.getLocation().getX())
                .categories(categories)
                .build();
    }

    @Transactional
    public PlaceResponse update(
            Long id,
            UpdatePlaceRequest request
    ) {
        Place place = placeRepository.findById(id)
                .orElseThrow(
                        () -> new PlaceNotFoundException(id)
                );

        Set<Category> categories =
                categoryRepository.findAllByCodeInAndActiveTrue(
                        request.getCategories()
                );

        validateCategories(
                request.getCategories(),
                categories
        );

        Map<String, Feature> features =
                featureService.resolveActive(
                        request.getFeatures().keySet()
                );

        Point location = geometryFactory.createPoint(
                new Coordinate(
                        request.getLongitude(),
                        request.getLatitude()
                )
        );

        place.update(
                request.getName(),
                request.getDescription(),
                request.getAddress(),
                location
        );

        place.updateVisitInfo(
                request.getVisitDurationMinutes(),
                request.getOpeningHours()
        );

        place.replaceCategories(categories);
        replaceFeatures(
                place,
                features,
                request.getFeatures()
        );

        replaceScores(
                place,
                request.getScores()
        );

        return toResponse(place);
    }

    @Transactional
    public void deactivate(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(
                        () -> new PlaceNotFoundException(id)
                );

        place.deactivate();
    }

    @Transactional
    public PlaceResponse activate(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(
                        () -> new PlaceNotFoundException(id)
                );

        place.activate();

        return toResponse(place);
    }

    public PageResponse<PlaceListItemResponse> findAll(
            int page,
            int size,
            Boolean active,
            PlaceSourceType sourceType,
            String category,
            String search
    ) {
        Specification<Place> specification =
                Specification.allOf(
                        hasActive(active),
                        hasSourceType(sourceType),
                        hasCategory(category),
                        nameContains(search)
                );

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "updatedAt"
                )
        );

        Page<PlaceListItemResponse> result =
                placeRepository
                        .findAll(
                                specification,
                                pageable
                        )
                        .map(this::toListItemResponse);

        return PageResponse.from(result);
    }

    private PlaceListItemResponse toListItemResponse(
            Place place
    ) {
        return PlaceListItemResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .address(place.getAddress())
                .latitude(
                        place.getLocation().getY()
                )
                .longitude(
                        place.getLocation().getX()
                )
                .sourceType(
                        place.getSourceType().name()
                )
                .active(place.isActive())
                .updatedAt(place.getUpdatedAt())
                .build();
    }

    private void replaceFeatures(
            Place place,
            Map<String, Feature> features,
            Map<String, Integer> values
    ) {
        placeFeatureRepository.deleteAllByPlaceId(
                place.getId()
        );

        if (values == null || values.isEmpty()) {
            return;
        }

        List<PlaceFeature> placeFeatures =
                values.entrySet()
                        .stream()
                        .map(entry ->
                                new PlaceFeature(
                                        place,
                                        features.get(
                                                entry.getKey()
                                        ),
                                        entry.getValue()
                                                .shortValue()
                                )
                        )
                        .toList();

        placeFeatureRepository.saveAll(
                placeFeatures
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

    private PlaceScoresResponse toScoresResponse(
            Long placeId
    ) {
        PlaceScore scores =
                placeScoreRepository
                        .findById(placeId)
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Scores not found for place "
                                                + placeId
                                )
                        );

        return PlaceScoresResponse.builder()
                .nature(scores.getNature())
                .attractions(scores.getAttractions())
                .military(scores.getMilitary())
                .religion(scores.getReligion())
                .architecture(scores.getArchitecture())
                .history(scores.getHistory())
                .art(scores.getArt())
                .souvenirs(scores.getSouvenirs())
                .transportTech(
                        scores.getTransportTech()
                )
                .accommodation(
                        scores.getAccommodation()
                )
                .food(scores.getFood())
                .exclusiveFood(
                        scores.getExclusiveFood()
                )
                .subcultures(
                        scores.getSubcultures()
                )
                .build();
    }
}