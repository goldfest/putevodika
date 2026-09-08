package ru.putevodika.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.security.web")
public record SecurityWebProperties(

        boolean cookieSecure,

        @NotBlank
        String cookieSameSite,

        @NotEmpty
        List<String> allowedOrigins
) {
}
