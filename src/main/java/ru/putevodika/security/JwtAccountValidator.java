package ru.putevodika.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import ru.putevodika.user.repository.UserRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAccountValidator
        implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_ACCOUNT =
            new OAuth2Error(
                    "invalid_token",
                    "Токен недействителен для текущего состояния учетной записи",
                    null
            );

    private final UserRepository userRepository;

    @Override
    public OAuth2TokenValidatorResult validate(
            Jwt jwt
    ) {
        Long userId;
        long tokenAuthVersion;

        try {
            userId = Long.valueOf(
                    jwt.getSubject()
            );

            Object claim =
                    jwt.getClaim(
                            "authVersion"
                    );

            if (!(claim instanceof Number number)) {
                return failure();
            }

            tokenAuthVersion =
                    number.longValue();

        } catch (Exception exception) {
            return failure();
        }

        Optional<Long> currentAuthVersion =
                userRepository
                        .findAuthVersionByIdAndActiveTrue(
                                userId
                        );

        if (currentAuthVersion.isEmpty()
                || currentAuthVersion.get()
                != tokenAuthVersion) {

            return failure();
        }

        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult failure() {
        return OAuth2TokenValidatorResult.failure(
                INVALID_ACCOUNT
        );
    }
}
