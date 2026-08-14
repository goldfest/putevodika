package ru.putevodika.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.putevodika.auth.service.AuthService;
import ru.putevodika.user.dto.RegisterRequest;
import ru.putevodika.user.dto.UserResponse;
import ru.putevodika.auth.dto.AuthTokenResponse;
import ru.putevodika.auth.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Авторизация",
        description = "Регистрация и вход пользователей"
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
}