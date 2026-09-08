package ru.putevodika.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import ru.putevodika.auth.config.PasswordResetProperties;
import ru.putevodika.auth.exception.InvalidPasswordResetTokenException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private static final String TOKEN_KEY_PREFIX =
            "password-reset:token:";

    private static final String USER_KEY_PREFIX =
            "password-reset:user:";

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private static final DefaultRedisScript<Long>
            CONSUME_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local tokenUser = redis.call('GET', KEYS[1])
                    local latestHash = redis.call('GET', KEYS[2])

                    if tokenUser ~= ARGV[1] then
                        return 0
                    end

                    if latestHash ~= ARGV[2] then
                        return 0
                    end

                    redis.call('DEL', KEYS[1])
                    redis.call('DEL', KEYS[2])

                    return 1
                    """,
                    Long.class
            );

    private final StringRedisTemplate redisTemplate;

    private final PasswordResetProperties properties;

    public IssuedToken issue(Long userId) {
        String rawToken = generateToken();
        String tokenHash = hash(rawToken);

        String userKey = userKey(userId);

        String previousHash =
                redisTemplate
                        .opsForValue()
                        .get(userKey);

        if (previousHash != null
                && !previousHash.isBlank()) {

            redisTemplate.delete(
                    tokenKey(previousHash)
            );
        }

        redisTemplate
                .opsForValue()
                .set(
                        tokenKey(tokenHash),
                        userId.toString(),
                        properties.tokenTtl()
                );

        redisTemplate
                .opsForValue()
                .set(
                        userKey,
                        tokenHash,
                        properties.tokenTtl()
                );

        return new IssuedToken(
                rawToken,
                tokenHash
        );
    }

    public boolean isValid(String rawToken) {
        if (!isTokenFormatValid(rawToken)) {
            return false;
        }

        String tokenHash = hash(rawToken);

        String userId =
                redisTemplate
                        .opsForValue()
                        .get(
                                tokenKey(tokenHash)
                        );

        if (userId == null) {
            return false;
        }

        String latestHash =
                redisTemplate
                        .opsForValue()
                        .get(
                                userKey(
                                        parseUserId(userId)
                                )
                        );

        return tokenHash.equals(latestHash);
    }

    public Long consume(String rawToken) {
        if (!isTokenFormatValid(rawToken)) {
            throw new InvalidPasswordResetTokenException();
        }

        String tokenHash = hash(rawToken);

        String tokenKey = tokenKey(tokenHash);

        String userId =
                redisTemplate
                        .opsForValue()
                        .get(tokenKey);

        if (userId == null) {
            throw new InvalidPasswordResetTokenException();
        }

        Long parsedUserId = parseUserId(userId);

        Long consumed =
                redisTemplate.execute(
                        CONSUME_SCRIPT,
                        List.of(
                                tokenKey,
                                userKey(parsedUserId)
                        ),
                        userId,
                        tokenHash
                );

        if (consumed == null || consumed != 1L) {
            throw new InvalidPasswordResetTokenException();
        }

        return parsedUserId;
    }

    public String generateUnusedTokenCandidate() {
        return generateToken();
    }

    private String generateToken() {
        byte[] bytes = new byte[32];

        SECURE_RANDOM.nextBytes(bytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private boolean isTokenFormatValid(
            String rawToken
    ) {
        return rawToken != null
                && rawToken.matches(
                        "^[A-Za-z0-9_-]{43}$"
                );
    }

    private String tokenKey(String tokenHash) {
        return TOKEN_KEY_PREFIX + tokenHash;
    }

    private String userKey(Long userId) {
        return USER_KEY_PREFIX + userId;
    }

    private Long parseUserId(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new InvalidPasswordResetTokenException();
        }
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

            return HexFormat
                    .of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 недоступен",
                    exception
            );
        }
    }

    public record IssuedToken(
            String rawToken,
            String tokenHash
    ) {
    }
}
