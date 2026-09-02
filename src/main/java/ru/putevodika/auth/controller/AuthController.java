package ru.putevodika.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.putevodika.auth.dto.AuthTokenResponse;
import ru.putevodika.auth.dto.LoginRequest;
import ru.putevodika.auth.dto.RefreshTokenRequest;
import ru.putevodika.auth.service.AuthService;
import ru.putevodika.user.dto.RegisterRequest;
import ru.putevodika.user.dto.UserResponse;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Авторизация",
        description = "Регистрация, вход и управление токенами"
)
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Регистрация пользователя"
    )
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @Operation(
            summary = "Авторизация пользователя"
    )
    @PostMapping("/login")
    public AuthTokenResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @Operation(
            summary = "Обновление токенов"
    )
    @PostMapping("/refresh")
    public AuthTokenResponse refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return authService.refresh(request);
    }

    @Operation(
            summary = "Выход из учетной записи"
    )
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(request);
    }
}