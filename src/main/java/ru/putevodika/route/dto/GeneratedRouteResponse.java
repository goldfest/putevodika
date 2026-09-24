package ru.putevodika.route.dto;

import lombok.Builder;
import lombok.Value;
import ru.putevodika.routing.dto.WalkingRouteResponse;

import java.time.LocalTime;
import java.util.List;

@Value
@Builder
public class GeneratedRouteResponse {

    RoutePointResponse start;

    RoutePointResponse finish;

    LocalTime startTime;

    LocalTime endTime;

    long availableDurationMinutes;

    long walkingDurationMinutes;

    long visitDurationMinutes;

    long totalDurationMinutes;

    List<RoutePlaceResponse> places;

    WalkingRouteResponse walkingRoute;
}