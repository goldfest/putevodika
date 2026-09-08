package ru.putevodika.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OnboardingResponse {

    UserResponse user;

    FeaturePreferencesResponse featurePreferences;
}
