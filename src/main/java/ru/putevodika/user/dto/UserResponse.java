package ru.putevodika.user.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Set;

@Value
@Builder
public class UserResponse {

    Long id;

    String email;

    String displayName;

    String avatarUrl;

    String role;

    boolean active;

    Set<String> preferredCategories;

    boolean onboardingCompleted;

    Instant onboardingCompletedAt;

    Instant createdAt;

    Instant updatedAt;
}
