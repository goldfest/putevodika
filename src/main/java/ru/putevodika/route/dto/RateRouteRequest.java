package ru.putevodika.route.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RateRouteRequest {

    @Min(1)
    @Max(5)
    private int rating;
}