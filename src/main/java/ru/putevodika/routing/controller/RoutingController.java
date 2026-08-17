package ru.putevodika.routing.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.putevodika.routing.dto.WalkingRouteResponse;
import ru.putevodika.routing.service.RoutingService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import ru.putevodika.routing.dto.WalkingMatrixRequest;
import ru.putevodika.routing.dto.WalkingMatrixResponse;

@RestController
@RequestMapping("/api/v1/routing")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Маршрутизация",
        description = "Построение пешеходных маршрутов"
)
public class RoutingController {

    private final RoutingService routingService;


    @GetMapping("/walking")
    public WalkingRouteResponse buildWalkingRoute(

            @RequestParam
            @DecimalMin("-90.0")
            @DecimalMax("90.0")
            double startLatitude,

            @RequestParam
            @DecimalMin("-180.0")
            @DecimalMax("180.0")
            double startLongitude,

            @RequestParam
            @DecimalMin("-90.0")
            @DecimalMax("90.0")
            double finishLatitude,

            @RequestParam
            @DecimalMin("-180.0")
            @DecimalMax("180.0")
            double finishLongitude
    ) {

        return routingService.buildWalkingRoute(
                startLatitude,
                startLongitude,
                finishLatitude,
                finishLongitude
        );
    }

    @PostMapping("/walking/matrix")
    public WalkingMatrixResponse buildWalkingMatrix(

            @Valid
            @RequestBody
            WalkingMatrixRequest request

    ) {

        return routingService.buildWalkingMatrix(
                request
        );
    }
}