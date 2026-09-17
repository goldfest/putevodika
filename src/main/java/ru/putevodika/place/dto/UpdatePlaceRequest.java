package ru.putevodika.place.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class UpdatePlaceRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    private String description;

    @Size(max = 500)
    private String address;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;

    @NotEmpty
    private Set<String> categories;

    @Min(1)
    @Max(1440)
    private Integer visitDurationMinutes;

    @Size(max = 255)
    private String openingHours;

    @NotNull
    @Size(max = 50)
    private Map<
                @NotBlank String,
                @NotNull
                @Min(0)
                @Max(5)
                        Integer
                > features = new HashMap<>();

    @NotNull
    @Valid
    private PlaceScoresRequest scores;

    @JsonProperty("available_for_route")
    private boolean availableForRoute = true;
}