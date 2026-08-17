package ru.putevodika.routing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WalkingMatrixRequest(

        @NotEmpty
        @Size(min = 2)
        List<@Valid RoutingPointRequest> points

) {
}