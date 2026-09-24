package ru.putevodika.route.dto;

import lombok.Builder;
import lombok.Value;
import ru.putevodika.routing.dto.WalkingRouteResponse;

import java.util.List;

@Value
@Builder
public class GeneratedRouteResponse {

    RoutePointResponse start;

    RoutePointResponse finish;

    List<RoutePlaceResponse> places;

    WalkingRouteResponse walkingRoute;
}