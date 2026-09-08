package ru.putevodika.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import ru.putevodika.user.entity.UserAccount;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;

    private final JwtProperties jwtProperties;

    public AccessToken createAccessToken(
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

        return new AccessToken(
                token,
                jwtProperties
                        .accessTokenTtl()
                        .toSeconds()
        );
    }

    public record AccessToken(
            String value,
            long expiresInSeconds
    ) {
    }
}
