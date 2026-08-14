package ru.putevodika.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.putevodika.user.dto.UserResponse;
import ru.putevodika.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.putevodika.user.dto.UpdatePreferencesRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import ru.putevodika.user.dto.UpdateProfileRequest;
import ru.putevodika.user.dto.ChangePasswordRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Пользователи",
        description = "Профиль текущего пользователя"
)
public class UserController {

    private final UserService userService;


    @GetMapping("/me")
    public UserResponse getCurrentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return userService.getById(
                currentUserId(jwt)
        );
    }

    @PatchMapping("/me")
    public UserResponse updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        Long userId = currentUserId(jwt);

        return userService.updateProfile(
                userId,
                request
        );
    }

    private Long currentUserId(Jwt jwt) {
        return Long.valueOf(
                jwt.getSubject()
        );
    }

    @PutMapping("/me/preferences")
    public UserResponse updatePreferences(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdatePreferencesRequest request
    ) {
        return userService.updatePreferences(
                currentUserId(jwt),
                request
        );
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(
                currentUserId(jwt),
                request
        );
    }
}