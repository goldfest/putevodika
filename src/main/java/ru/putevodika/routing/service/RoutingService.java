package ru.putevodika.routing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.putevodika.routing.client.OsrmClient;
import ru.putevodika.routing.client.OsrmRouteResult;
import ru.putevodika.routing.dto.WalkingRouteResponse;
import java.util.List;

import ru.putevodika.routing.client.OsrmMatrixResult;
import ru.putevodika.routing.client.RoutingPoint;
import ru.putevodika.routing.dto.WalkingMatrixRequest;
import ru.putevodika.routing.dto.WalkingMatrixResponse;

@Service
@RequiredArgsConstructor
public class RoutingService {

    private final OsrmClient osrmClient;


    public WalkingRouteResponse buildWalkingRoute(
            double startLatitude,
            double startLongitude,
            double finishLatitude,
            double finishLongitude
    ) {

        OsrmRouteResult result =
                osrmClient.buildWalkingRoute(
                        startLatitude,
                        startLongitude,
                        finishLatitude,
                        finishLongitude
                );


        return WalkingRouteResponse.builder()
                .distanceMeters(
                        result.distanceMeters()
                )
                .durationSeconds(
                        result.durationSeconds()
                )
                .geometryType(
                        result.geometryType()
                )
                .coordinates(
                        result.coordinates()
                )
                .build();
    }

    public WalkingMatrixResponse buildWalkingMatrix(
            WalkingMatrixRequest request
    ) {

        List<RoutingPoint> points =
                request.points()
                        .stream()
                        .map(point ->
                                new RoutingPoint(
                                        point.latitude(),
                                        point.longitude()
                                )
                        )
                        .toList();

        OsrmMatrixResult result =
                osrmClient.buildWalkingMatrix(points);

        return new WalkingMatrixResponse(
                result.durationsSeconds(),
                result.distancesMeters()
        );
    }
}