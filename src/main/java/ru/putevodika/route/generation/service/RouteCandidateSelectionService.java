package ru.putevodika.route.generation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.putevodika.place.entity.Place;
import ru.putevodika.place.repository.PlaceRepository;
import ru.putevodika.route.dto.GenerateRouteRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteCandidateSelectionService {

    /*
     * Ограничение необходимо, потому что OSRM Table
     * строит квадратную матрицу N x N.
     *
     * Сейчас передаём максимум 100 кандидатов.
     */
    private static final int MAX_CANDIDATE_COUNT = 100;

    private static final int MIN_SEARCH_RADIUS_METERS =
            5_000;

    private static final int EXTRA_SEARCH_RADIUS_METERS =
            3_000;

    private static final int MAX_SEARCH_RADIUS_METERS =
            50_000;

    private static final double EARTH_RADIUS_METERS =
            6_371_008.8;

    private final PlaceRepository placeRepository;


    public List<Place> selectCandidates(
            GenerateRouteRequest request
    ) {

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

        return placeRepository
                .findRouteCandidatesNearby(
                        midpointLatitude,
                        midpointLongitude,
                        radiusMeters,
                        MAX_CANDIDATE_COUNT
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

        /*
         * Радиус:
         *
         * половина расстояния start -> finish
         * + дополнительная зона поиска.
         *
         * Таким образом start и finish попадают
         * внутрь области кандидатов.
         */
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
}