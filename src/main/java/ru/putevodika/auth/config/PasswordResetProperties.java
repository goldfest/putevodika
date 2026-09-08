package ru.putevodika.auth.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.auth.password-reset")
public record PasswordResetProperties(

        @NotNull
        Duration tokenTtl,

        @NotBlank
        String frontendResetUrl,

        @NotBlank
        String mailFrom,

        @Min(1)
        int emailMaxRequests,

        @Min(1)
        int ipMaxRequests,

        @NotNull
        Duration rateWindow
) {
}
