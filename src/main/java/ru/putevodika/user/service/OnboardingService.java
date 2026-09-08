package ru.putevodika.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.user.dto.FeaturePreferencesResponse;
import ru.putevodika.user.dto.OnboardingRequest;
import ru.putevodika.user.dto.OnboardingResponse;
import ru.putevodika.user.dto.UserResponse;
import ru.putevodika.user.entity.UserAccount;
import ru.putevodika.user.exception.UserNotFoundException;
import ru.putevodika.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingService {

    private final UserRepository userRepository;

    private final UserService userService;

    private final UserFeaturePreferenceService
            userFeaturePreferenceService;

    @Transactional
    public OnboardingResponse complete(
            Long userId,
            OnboardingRequest request
    ) {
        userService.replacePreferences(
                userId,
                request.getCategories()
        );

        FeaturePreferencesResponse
                featurePreferences =
                userFeaturePreferenceService.replace(
                        userId,
                        request.getFeaturePreferences()
                );

        UserAccount user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new UserNotFoundException(
                                                userId
                                        )
                        );

        user.completeOnboarding();

        UserResponse userResponse =
                userService.getById(userId);

        return OnboardingResponse
                .builder()
                .user(userResponse)
                .featurePreferences(
                        featurePreferences
                )
                .build();
    }
}
