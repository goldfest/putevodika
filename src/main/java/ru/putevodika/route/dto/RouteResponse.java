package ru.putevodika.route.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class RouteResponse {

    Long id;

    RoutePointResponse start;

    RoutePointResponse finish;

    Integer walkingDistanceMeters;

    Integer walkingDurationSeconds;

    Integer totalDurationMinutes;

    Integer rating;

    List<RoutePlaceResponse> places;

    Instant createdAt;

    Instant updatedAt;
}