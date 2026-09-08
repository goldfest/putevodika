package ru.putevodika.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.putevodika.auth.config.PasswordResetProperties;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PasswordResetRateLimiter {

    private static final String EMAIL_KEY_PREFIX =
            "password-reset:rate:email:";

    private static final String IP_KEY_PREFIX =
            "password-reset:rate:ip:";

    private final StringRedisTemplate redisTemplate;

    private final PasswordResetProperties properties;

    public boolean allowEmail(String normalizedEmail) {
        return allow(
                EMAIL_KEY_PREFIX
                        + hash(normalizedEmail),
                properties.emailMaxRequests()
        );
    }

    public boolean allowIp(String remoteAddress) {
        String address =
                remoteAddress == null
                        ? "unknown"
                        : remoteAddress;

        return allow(
                IP_KEY_PREFIX
                        + hash(address),
                properties.ipMaxRequests()
        );
    }

    private boolean allow(
            String key,
            int maxRequests
    ) {
        Long count =
                redisTemplate
                        .opsForValue()
                        .increment(key);

        if (count == null) {
            return false;
        }

        if (count == 1L) {
            redisTemplate.expire(
                    key,
                    properties.rateWindow()
            );
        }

        return count <= maxRequests;
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
}
