package ru.putevodika.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import ru.putevodika.auth.dto.AuthTokenResponse;
import ru.putevodika.user.entity.UserAccount;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;

    private final JwtProperties jwtProperties;


    public AuthTokenResponse createAccessToken(
            UserAccount user
    ) {
        Instant now = Instant.now();

        Instant expiresAt = now.plus(
                jwtProperties.accessTokenTtl()
        );

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(
                                jwtProperties.issuer()
                        )
                        .issuedAt(now)
                        .expiresAt(expiresAt)
                        .subject(
                                user.getId().toString()
                        )
                        .claim(
                                "email",
                                user.getEmail()
                        )
                        .claim(
                                "roles",
                                List.of(
                                        user.getRole().name()
                                )
                        )
                        .build();

        String token =
                jwtEncoder.encode(
                                JwtEncoderParameters.from(
                                        claims
                                )
                        )
                        .getTokenValue();

        return AuthTokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInSeconds(
                        jwtProperties
                                .accessTokenTtl()
                                .toSeconds()
                )
                .build();
    }
}