package ru.putevodika.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @Size(max = 100)
    @Pattern(
            regexp = ".*\\S.*",
            message = "displayName не может быть пустым"
    )
    private String displayName;

    @Size(max = 2048)
    @Pattern(
            regexp = "^(https?://\\S+)?$",
            message = "avatarUrl должен быть HTTP или HTTPS URL"
    )
    private String avatarUrl;
}