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

import java.util.HashSet;
import java.util.List;
import ru.putevodika.place.exception.PlaceNotFoundException;
import ru.putevodika.place.exception.UnknownPlaceCategoryException;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private static final double EARTH_RADIUS_METERS =
            6_371_008.8;

    private final PlaceRepository placeRepository;

    private final CategoryRepository categoryRepository;

    private final GeometryFactory geometryFactory;


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

        categories.forEach(place::addCategory);

        Place saved = placeRepository.save(place);

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

        place.replaceCategories(categories);

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
}