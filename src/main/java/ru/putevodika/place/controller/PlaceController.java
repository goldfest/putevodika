package ru.putevodika.place.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.putevodika.place.dto.CreatePlaceRequest;
import ru.putevodika.place.dto.PlaceResponse;
import ru.putevodika.place.service.PlaceService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import ru.putevodika.place.dto.NearbyPlaceResponse;
import ru.putevodika.place.dto.MapPlaceResponse;
import ru.putevodika.place.dto.UpdatePlaceRequest;

import java.util.List;
import java.util.Set;
import java.util.List;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@Validated
public class PlaceController {

    private final PlaceService placeService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceResponse create(
            @Valid @RequestBody CreatePlaceRequest request
    ) {
        return placeService.create(request);
    }


    @GetMapping("/{id}")
    public PlaceResponse getById(
            @PathVariable Long id
    ) {
        return placeService.getById(id);
    }

    @GetMapping("/nearby")
    public List<NearbyPlaceResponse> findNearby(
            @RequestParam
            @DecimalMin("-90.0")
            @DecimalMax("90.0")
            double latitude,

            @RequestParam
            @DecimalMin("-180.0")
            @DecimalMax("180.0")
            double longitude,

            @RequestParam(defaultValue = "5000")
            @Min(1)
            @Max(100000)
            int radiusMeters,

            @RequestParam(required = false)
            Set<String> categories,

            @RequestParam(defaultValue = "50")
            @Min(1)
            @Max(200)
            int limit
    ) {
        return placeService.findNearby(
                latitude,
                longitude,
                radiusMeters,
                categories,
                limit
        );
    }

    @GetMapping("/in-bounds")
    public List<MapPlaceResponse> findInBounds(
            @RequestParam
            @DecimalMin("-90.0")
            @DecimalMax("90.0")
            double minLatitude,

            @RequestParam
            @DecimalMin("-180.0")
            @DecimalMax("180.0")
            double minLongitude,

            @RequestParam
            @DecimalMin("-90.0")
            @DecimalMax("90.0")
            double maxLatitude,

            @RequestParam
            @DecimalMin("-180.0")
            @DecimalMax("180.0")
            double maxLongitude,

            @RequestParam(required = false)
            Set<String> categories,

            @RequestParam(defaultValue = "500")
            @Min(1)
            @Max(2000)
            int limit
    ) {
        return placeService.findInBounds(
                minLatitude,
                minLongitude,
                maxLatitude,
                maxLongitude,
                categories,
                limit
        );
    }

    @PutMapping("/{id}")
    public PlaceResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlaceRequest request
    ) {
        return placeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(
            @PathVariable Long id
    ) {
        placeService.deactivate(id);
    }

    @PatchMapping("/{id}/activate")
    public PlaceResponse activate(
            @PathVariable Long id
    ) {
        return placeService.activate(id);
    }
}