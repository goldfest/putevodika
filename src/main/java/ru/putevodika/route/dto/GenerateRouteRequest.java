package ru.putevodika.route.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.putevodika.routing.dto.RoutingPointRequest;

public record GenerateRouteRequest(

        @NotNull
        @Valid
        RoutingPointRequest start,

        @NotNull
        @Valid
        RoutingPointRequest finish

) {
}