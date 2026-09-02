package ru.putevodika.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.auth.entity.RefreshToken;
import ru.putevodika.auth.exception.InvalidRefreshTokenException;
import ru.putevodika.auth.repository.RefreshTokenRepository;
import ru.putevodika.security.JwtProperties;
import ru.putevodika.user.entity.UserAccount;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final RefreshTokenRepository
            refreshTokenRepository;

    private final JwtProperties jwtProperties;

    @Transactional
    public String issue(UserAccount user) {
        String rawToken = generateToken();

        RefreshToken refreshToken =
                new RefreshToken(
                        user,
                        hash(rawToken),
                        Instant.now().plus(
                                jwtProperties.refreshTokenTtl()
                        )
                );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public RotatedRefreshToken rotate(
            String rawToken
    ) {
        Instant now = Instant.now();

        RefreshToken storedToken =
                refreshTokenRepository
                        .findByTokenHashForUpdate(
                                hash(rawToken)
                        )
                        .orElseThrow(
                                InvalidRefreshTokenException::new
                        );

        if (storedToken.isRevoked()
                || storedToken.isExpired(now)
                || !storedToken.getUser().isActive()) {

            throw new InvalidRefreshTokenException();
        }

        storedToken.revoke(now);

        String newRawToken = generateToken();

        RefreshToken newToken =
                new RefreshToken(
                        storedToken.getUser(),
                        hash(newRawToken),
                        now.plus(
                                jwtProperties.refreshTokenTtl()
                        )
                );

        refreshTokenRepository.save(newToken);

        return new RotatedRefreshToken(
                storedToken.getUser(),
                newRawToken
        );
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository
                .findByTokenHashForUpdate(
                        hash(rawToken)
                )
                .ifPresent(token ->
                        token.revoke(Instant.now())
                );
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository
                .revokeAllActiveByUserId(
                        userId,
                        Instant.now()
                );
    }

    public long getRefreshTokenTtlSeconds() {
        return jwtProperties
                .refreshTokenTtl()
                .toSeconds();
    }

    private String generateToken() {
        byte[] bytes = new byte[32];

        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 недоступен",
                    exception
            );
        }
    }

    public record RotatedRefreshToken(
            UserAccount user,
            String refreshToken
    ) {
    }
}