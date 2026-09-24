package ru.putevodika.route.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.putevodika.place.entity.Place;
import ru.putevodika.place.repository.PlaceRepository;
import ru.putevodika.route.dto.GenerateRouteRequest;
import ru.putevodika.route.dto.GeneratedRouteResponse;
import ru.putevodika.route.dto.RoutePlaceResponse;
import ru.putevodika.route.dto.RoutePointResponse;
import ru.putevodika.route.exception.InvalidRouteTimeWindowException;
import ru.putevodika.route.exception.RouteTimeLimitExceededException;
import ru.putevodika.routing.client.RoutingPoint;
import ru.putevodika.routing.dto.WalkingRouteResponse;
import ru.putevodika.routing.service.RoutingService;
import ru.putevodika.user.service.UserFeaturePreferenceService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RouteGenerationService {

    private static final int STUB_PLACE_COUNT = 3;

    private static final int MIN_SEARCH_RADIUS_METERS =
            5_000;

    private static final int EXTRA_SEARCH_RADIUS_METERS =
            3_000;

    private static final int MAX_SEARCH_RADIUS_METERS =
            50_000;

    private static final double EARTH_RADIUS_METERS =
            6_371_008.8;

    private final PlaceRepository placeRepository;

    private final RoutingService routingService;

    private final UserFeaturePreferenceService
            userFeaturePreferenceService;


    public GeneratedRouteResponse generate(
            Long userId,
            GenerateRouteRequest request
    ) {
        validateTimeWindow(request);

        Map<String, Integer> preferences =
                userFeaturePreferenceService
                        .get(userId)
                        .getPreferences();

        double midpointLatitude =
                (
                        request.start().latitude()
                                + request.finish().latitude()
                ) / 2.0;

        double midpointLongitude =
                (
                        request.start().longitude()
                                + request.finish().longitude()
                ) / 2.0;

        int radiusMeters =
                calculateSearchRadiusMeters(
                        request
                );

        List<Place> selectedPlaces =
                selectStubPlaces(
                        midpointLatitude,
                        midpointLongitude,
                        radiusMeters,
                        preferences
                );

        RoutePlan routePlan =
                fitIntoTimeWindow(
                        request,
                        selectedPlaces
                );

        List<RoutePlaceResponse> places =
                IntStream.range(
                                0,
                                routePlan.places().size()
                        )
                        .mapToObj(index ->
                                toRoutePlaceResponse(
                                        routePlan
                                                .places()
                                                .get(index),
                                        index + 1
                                )
                        )
                        .toList();

        long availableDurationMinutes =
                Duration.between(
                                request.startTime(),
                                request.endTime()
                        )
                        .toMinutes();

        long walkingDurationMinutes =
                secondsToMinutesCeil(
                        routePlan
                                .walkingRoute()
                                .getDurationSeconds()
                );

        return GeneratedRouteResponse.builder()
                .start(
                        RoutePointResponse.builder()
                                .latitude(
                                        request.start().latitude()
                                )
                                .longitude(
                                        request.start().longitude()
                                )
                                .build()
                )
                .finish(
                        RoutePointResponse.builder()
                                .latitude(
                                        request.finish().latitude()
                                )
                                .longitude(
                                        request.finish().longitude()
                                )
                                .build()
                )
                .startTime(
                        request.startTime()
                )
                .endTime(
                        request.endTime()
                )
                .availableDurationMinutes(
                        availableDurationMinutes
                )
                .walkingDurationMinutes(
                        walkingDurationMinutes
                )
                .visitDurationMinutes(
                        routePlan.visitDurationMinutes()
                )
                .totalDurationMinutes(
                        routePlan.totalDurationMinutes()
                )
                .places(places)
                .walkingRoute(
                        routePlan.walkingRoute()
                )
                .build();
    }


    private RoutePlan fitIntoTimeWindow(
            GenerateRouteRequest request,
            List<Place> selectedPlaces
    ) {
        long availableSeconds =
                Duration.between(
                                request.startTime(),
                                request.endTime()
                        )
                        .toSeconds();

        List<Place> places =
                new ArrayList<>(
                        selectedPlaces
                );

        while (true) {
            WalkingRouteResponse walkingRoute =
                    buildWalkingRoute(
                            request,
                            places
                    );

            long visitDurationMinutes =
                    calculateVisitDurationMinutes(
                            places
                    );

            double totalDurationSeconds =
                    walkingRoute.getDurationSeconds()
                            + visitDurationMinutes * 60.0;

            if (totalDurationSeconds
                    <= availableSeconds) {

                return new RoutePlan(
                        List.copyOf(places),
                        walkingRoute,
                        visitDurationMinutes,
                        secondsToMinutesCeil(
                                totalDurationSeconds
                        )
                );
            }

            if (places.isEmpty()) {
                throw new RouteTimeLimitExceededException(
                        Duration.between(
                                        request.startTime(),
                                        request.endTime()
                                )
                                .toMinutes(),
                        secondsToMinutesCeil(
                                walkingRoute
                                        .getDurationSeconds()
                        )
                );
            }

            places.remove(
                    places.size() - 1
            );
        }
    }


    private WalkingRouteResponse buildWalkingRoute(
            GenerateRouteRequest request,
            List<Place> places
    ) {
        List<RoutingPoint> routingPoints =
                new ArrayList<>();

        routingPoints.add(
                new RoutingPoint(
                        request.start().latitude(),
                        request.start().longitude()
                )
        );

        places.forEach(place ->
                routingPoints.add(
                        new RoutingPoint(
                                place.getLocation().getY(),
                                place.getLocation().getX()
                        )
                )
        );

        routingPoints.add(
                new RoutingPoint(
                        request.finish().latitude(),
                        request.finish().longitude()
                )
        );

        return routingService.buildWalkingRoute(
                routingPoints
        );
    }


    private long calculateVisitDurationMinutes(
            List<Place> places
    ) {
        return places.stream()
                .map(
                        Place::getVisitDurationMinutes
                )
                .filter(Objects::nonNull)
                .mapToLong(
                        Integer::longValue
                )
                .sum();
    }


    private void validateTimeWindow(
            GenerateRouteRequest request
    ) {
        if (!request.endTime()
                .isAfter(request.startTime())) {

            throw new InvalidRouteTimeWindowException();
        }
    }


    private long secondsToMinutesCeil(
            double seconds
    ) {
        return (long) Math.ceil(
                seconds / 60.0
        );
    }


    private List<Place> selectStubPlaces(
            double latitude,
            double longitude,
            int radiusMeters,
            Map<String, Integer> preferences
    ) {
        /*
         * Заглушка.
         *
         * preferences уже получены из БД,
         * но пока намеренно не используются.
         *
         * Позже здесь будет вызов
         * генетического алгоритма.
         */
        return placeRepository
                .findRandomAvailableForRouteNearby(
                        latitude,
                        longitude,
                        radiusMeters,
                        STUB_PLACE_COUNT
                );
    }


    private int calculateSearchRadiusMeters(
            GenerateRouteRequest request
    ) {
        double distance =
                calculateDistanceMeters(
                        request.start().latitude(),
                        request.start().longitude(),
                        request.finish().latitude(),
                        request.finish().longitude()
                );

        int radius =
                (int) Math.ceil(
                        distance / 2.0
                                + EXTRA_SEARCH_RADIUS_METERS
                );

        return Math.min(
                MAX_SEARCH_RADIUS_METERS,
                Math.max(
                        MIN_SEARCH_RADIUS_METERS,
                        radius
                )
        );
    }


    private double calculateDistanceMeters(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2
    ) {
        double latitudeDelta =
                Math.toRadians(
                        latitude2 - latitude1
                );

        double longitudeDelta =
                Math.toRadians(
                        longitude2 - longitude1
                );

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

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return EARTH_RADIUS_METERS * c;
    }


    private RoutePlaceResponse toRoutePlaceResponse(
            Place place,
            int position
    ) {
        return RoutePlaceResponse.builder()
                .id(place.getId())
                .position(position)
                .name(place.getName())
                .address(place.getAddress())
                .latitude(
                        place.getLocation().getY()
                )
                .longitude(
                        place.getLocation().getX()
                )
                .build();
    }


    private record RoutePlan(
            List<Place> places,
            WalkingRouteResponse walkingRoute,
            long visitDurationMinutes,
            long totalDurationMinutes
    ) {
    }
}