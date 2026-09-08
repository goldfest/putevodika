package ru.putevodika.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import ru.putevodika.auth.dto.LoginRequest;
import ru.putevodika.auth.exception.InvalidRefreshTokenException;
import ru.putevodika.auth.service.AuthCookieService;
import ru.putevodika.auth.service.AuthService;
import ru.putevodika.user.dto.RegisterRequest;
import ru.putevodika.user.dto.UserResponse;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Авторизация",
        description = "Регистрация, вход и управление сессией"
)
public class AuthController {

    private final AuthService authService;

    private final AuthCookieService authCookieService;

    @Operation(
            summary = "Получить CSRF-токен"
    )
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

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
    public UserResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthService.AuthSession session =
                authService.login(request);

        authCookieService.writeSessionCookies(
                response,
                session.accessToken(),
                session.refreshToken()
        );

        return session.user();
    }

    @Operation(
            summary = "Обновление сессии"
    )
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refresh(
            @CookieValue(
                    name = AuthCookieService.REFRESH_TOKEN_COOKIE,
                    required = false
            )
            String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        AuthService.AuthSession session =
                authService.refresh(refreshToken);

        authCookieService.writeSessionCookies(
                response,
                session.accessToken(),
                session.refreshToken()
        );
    }

    @Operation(
            summary = "Выход из учетной записи"
    )
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @CookieValue(
                    name = AuthCookieService.REFRESH_TOKEN_COOKIE,
                    required = false
            )
            String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }

        authCookieService.clearSessionCookies(response);
    }
}
