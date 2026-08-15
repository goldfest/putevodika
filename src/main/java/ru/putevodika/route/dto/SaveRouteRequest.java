package ru.putevodika.route.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaveRouteRequest {

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double startLatitude;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double startLongitude;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double finishLatitude;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double finishLongitude;

    @NotEmpty
    @Size(max = 50)
    private List<
            @NotNull
            @Positive
                    Long
            > placeIds;
}