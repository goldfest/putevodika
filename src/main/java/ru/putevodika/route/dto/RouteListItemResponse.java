package ru.putevodika.route.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class RouteListItemResponse {

    Long id;

    RoutePointResponse start;

    RoutePointResponse finish;

    Integer walkingDistanceMeters;

    Integer walkingDurationSeconds;

    Integer totalDurationMinutes;

    Instant createdAt;
}