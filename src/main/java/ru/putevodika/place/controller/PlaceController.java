package ru.putevodika.place.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.putevodika.place.dto.CreatePlaceRequest;
import ru.putevodika.place.dto.PlaceResponse;
import ru.putevodika.place.service.PlaceService;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
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
}