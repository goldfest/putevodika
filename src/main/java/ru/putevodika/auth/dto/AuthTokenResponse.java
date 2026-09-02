package ru.putevodika.auth.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthTokenResponse {

    String accessToken;

    String refreshToken;

    String tokenType;

    long accessExpiresInSeconds;

    long refreshExpiresInSeconds;
}