package ru.putevodika.route.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RouteRatingResponse {

    Long routeId;

    int rating;
}