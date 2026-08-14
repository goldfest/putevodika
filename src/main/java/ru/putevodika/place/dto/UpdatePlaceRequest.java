package ru.putevodika.place.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

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
}