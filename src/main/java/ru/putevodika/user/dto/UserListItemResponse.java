package ru.putevodika.user.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class UserListItemResponse {

    Long id;

    String email;

    String displayName;

    String avatarUrl;

    String role;

    boolean active;

    Instant createdAt;

    Instant updatedAt;
}
