package ru.putevodika.routing.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class WalkingRouteResponse {

    double distanceMeters;

    double durationSeconds;

    String geometryType;

    List<List<Double>> coordinates;
}