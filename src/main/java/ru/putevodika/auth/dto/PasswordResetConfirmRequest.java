package ru.putevodika.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetConfirmRequest {

    @NotBlank
    @Pattern(
            regexp = "^[A-Za-z0-9_-]{43}$",
            message = "Некорректный формат токена восстановления"
    )
    private String token;

    @NotBlank
    @Size(min = 8, max = 128)
    private String newPassword;

    @NotBlank
    @Size(min = 8, max = 128)
    private String repeatPassword;
}
