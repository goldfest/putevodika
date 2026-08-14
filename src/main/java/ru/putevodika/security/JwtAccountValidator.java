package ru.putevodika.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import ru.putevodika.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class JwtAccountValidator
        implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_ACCOUNT =
            new OAuth2Error(
                    "invalid_token",
                    "Пользователь не найден или учетная запись отключена",
                    null
            );

    private final UserRepository userRepository;


    @Override
    public OAuth2TokenValidatorResult validate(
            Jwt jwt
    ) {
        Long userId;

        try {
            userId = Long.valueOf(
                    jwt.getSubject()
            );
        } catch (Exception exception) {
            return OAuth2TokenValidatorResult.failure(
                    INVALID_ACCOUNT
            );
        }

        if (!userRepository.existsByIdAndActiveTrue(
                userId
        )) {
            return OAuth2TokenValidatorResult.failure(
                    INVALID_ACCOUNT
            );
        }

        return OAuth2TokenValidatorResult.success();
    }
}