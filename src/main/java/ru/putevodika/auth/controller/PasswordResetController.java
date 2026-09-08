package ru.putevodika.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.putevodika.auth.dto.PasswordResetConfirmRequest;
import ru.putevodika.auth.dto.PasswordResetRequest;
import ru.putevodika.auth.dto.PasswordResetRequestResponse;
import ru.putevodika.auth.dto.PasswordResetValidationResponse;
import ru.putevodika.auth.service.AuthCookieService;
import ru.putevodika.auth.service.PasswordResetService;

@RestController
@RequestMapping(
        "/api/v1/auth/password-reset"
)
@RequiredArgsConstructor
@Tag(
        name = "Восстановление доступа",
        description = "Восстановление пароля через одноразовую ссылку"
)
public class PasswordResetController {

    private static final String GENERIC_MESSAGE =
            "Если указанная почта зарегистрирована, "
                    + "мы отправили ссылку для восстановления пароля";

    private final PasswordResetService
            passwordResetService;

    private final AuthCookieService
            authCookieService;

    @Operation(
            summary = "Запросить восстановление пароля"
    )
    @PostMapping("/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PasswordResetRequestResponse request(
            @Valid
            @RequestBody
            PasswordResetRequest request,
            HttpServletRequest servletRequest
    ) {
        passwordResetService.requestReset(
                request.getEmail(),
                servletRequest.getRemoteAddr()
        );

        return new PasswordResetRequestResponse(
                GENERIC_MESSAGE
        );
    }

    @Operation(
            summary = "Проверить ссылку восстановления"
    )
    @GetMapping("/validate")
    public PasswordResetValidationResponse validate(
            @RequestParam String token
    ) {
        return new PasswordResetValidationResponse(
                passwordResetService
                        .validateToken(token)
        );
    }

    @Operation(
            summary = "Установить новый пароль"
    )
    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirm(
            @Valid
            @RequestBody
            PasswordResetConfirmRequest request,
            HttpServletResponse response
    ) {
        passwordResetService.confirmReset(
                request
        );

        authCookieService.clearSessionCookies(
                response
        );
    }
}
