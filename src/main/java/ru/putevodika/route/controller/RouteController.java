package ru.putevodika.route.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.putevodika.common.dto.PageResponse;
import ru.putevodika.route.dto.*;
import ru.putevodika.route.service.RouteService;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Маршруты",
        description = "Сохраненные туристические маршруты"
)
public class RouteController {

    private final RouteService routeService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RouteResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid
            @RequestBody
            SaveRouteRequest request
    ) {
        return routeService.create(
                currentUserId(jwt),
                request
        );
    }


    @GetMapping
    public PageResponse<RouteListItemResponse>
    findAll(
            @AuthenticationPrincipal Jwt jwt,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size
    ) {
        return routeService.findAll(
                currentUserId(jwt),
                page,
                size
        );
    }


    @GetMapping("/{id}")
    public RouteResponse getById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        return routeService.getById(
                currentUserId(jwt),
                id
        );
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        routeService.delete(
                currentUserId(jwt),
                id
        );
    }


    @PutMapping("/{id}/rating")
    public RouteRatingResponse rate(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid
            @RequestBody
            RateRouteRequest request
    ) {
        return routeService.rate(
                currentUserId(jwt),
                id,
                request
        );
    }


    private Long currentUserId(
            Jwt jwt
    ) {
        return Long.valueOf(
                jwt.getSubject()
        );
    }
}