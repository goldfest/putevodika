package ru.putevodika.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UpdatePreferencesRequest {

    @NotNull
    @Size(max = 20)
    private Set<String> categories;
}