package ru.putevodika.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.putevodika.auth.service.AuthCookieService;
import ru.putevodika.user.dto.ChangePasswordRequest;
import ru.putevodika.user.dto.FeaturePreferencesResponse;
import ru.putevodika.user.dto.OnboardingRequest;
import ru.putevodika.user.dto.OnboardingResponse;
import ru.putevodika.user.dto.UpdateFeaturePreferencesRequest;
import ru.putevodika.user.dto.UpdatePreferencesRequest;
import ru.putevodika.user.dto.UpdateProfileRequest;
import ru.putevodika.user.dto.UserResponse;
import ru.putevodika.user.service.OnboardingService;
import ru.putevodika.user.service.UserFeaturePreferenceService;
import ru.putevodika.user.service.UserService;

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

    private final UserFeaturePreferenceService
            userFeaturePreferenceService;

    private final OnboardingService
            onboardingService;

    private final AuthCookieService
            authCookieService;

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

    @Operation(
            summary = "Завершить первичную настройку пользователя"
    )
    @PostMapping("/me/onboarding")
    public OnboardingResponse completeOnboarding(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OnboardingRequest request
    ) {
        return onboardingService.complete(
                currentUserId(jwt),
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
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletResponse response
    ) {
        userService.changePassword(
                currentUserId(jwt),
                request
        );

        authCookieService.clearSessionCookies(
                response
        );
    }

    @GetMapping("/me/feature-preferences")
    public FeaturePreferencesResponse
    getFeaturePreferences(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return userFeaturePreferenceService.get(
                currentUserId(jwt)
        );
    }

    @PutMapping("/me/feature-preferences")
    public FeaturePreferencesResponse
    updateFeaturePreferences(
            @AuthenticationPrincipal Jwt jwt,
            @Valid
            @RequestBody
            UpdateFeaturePreferencesRequest request
    ) {
        return userFeaturePreferenceService.replace(
                currentUserId(jwt),
                request
        );
    }
}
