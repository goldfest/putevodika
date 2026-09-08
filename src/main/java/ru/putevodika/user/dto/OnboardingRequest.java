package ru.putevodika.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class OnboardingRequest {

    @NotNull
    @Size(max = 20)
    private Set<
            @NotBlank
                    String
            > categories;

    @NotNull
    @Size(max = 50)
    private Map<
            @NotBlank String,
            @NotNull
            @Min(0)
            @Max(5)
                    Integer
            > featurePreferences;
}
