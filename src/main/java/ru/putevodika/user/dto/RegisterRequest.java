package ru.putevodika.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    @Pattern(
            regexp = "^[\\p{L}\\p{N}._-]+$",
            message = "Логин может содержать только буквы, цифры, точку, дефис и нижнее подчеркивание"
    )
    private String login;

    @NotBlank
    @Size(min = 8, max = 128)
    private String password;

    @NotBlank
    @Size(max = 100)
    private String displayName;

    @Size(max = 2048)
    @Pattern(
            regexp = "^(https?://\\S+)?$",
            message = "avatarUrl должен быть HTTP или HTTPS URL"
    )
    private String avatarUrl;

    @Size(max = 20)
    private Set<String> preferredCategories =
            new HashSet<>();
}