package ru.putevodika.admin.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * Ограничение неудачных попыток входа в административную панель.
 * Redis обеспечивает общие счётчики для нескольких экземпляров backend.
 */
@Service
@RequiredArgsConstructor
public class AdminLoginRateLimiter {

    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int MAX_FAILURES_BY_IP = 60;
    private static final int MAX_FAILURES_BY_IP_AND_EMAIL = 6;

    // INCR + EXPIRE выполняются атомарно, чтобы ключ не оставался без TTL.
    private static final DefaultRedisScript<Long> INCREMENT_SCRIPT =
            new DefaultRedisScript<>("""
                    local count = redis.call('INCR', KEYS[1]);
                    if count == 1 then
                        redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]));
                    end;
                    return count;
                    """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public boolean canAttempt(String email, String remoteAddress) {
        return count(ipKey(remoteAddress)) < MAX_FAILURES_BY_IP
                && count(pairKey(email, remoteAddress)) < MAX_FAILURES_BY_IP_AND_EMAIL;
    }

    public void recordFailure(String email, String remoteAddress) {
        increment(ipKey(remoteAddress));
        increment(pairKey(email, remoteAddress));
    }

    public void recordSuccess(String email, String remoteAddress) {
        // Счётчик общего числа ошибок IP сохраняется до конца окна:
        // успешный вход не должен сбрасывать защиту от перебора других аккаунтов.
        redisTemplate.delete(pairKey(email, remoteAddress));
    }

    private long count(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? 0L : Long.parseLong(value);
    }

    private void increment(String key) {
        redisTemplate.execute(INCREMENT_SCRIPT, List.of(key),
                Long.toString(WINDOW.toSeconds()));
    }

    private String ipKey(String remoteAddress) {
        return "admin:login:ip:" + sha256(normalizeAddress(remoteAddress));
    }

    private String pairKey(String email, String remoteAddress) {
        return "admin:login:pair:" + sha256(
                normalizeAddress(remoteAddress) + "|" +
                        email.trim().toLowerCase(Locale.ROOT));
    }

    private static String normalizeAddress(String address) {
        return address == null || address.isBlank() ? "unknown" : address.trim();
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 недоступен", exception);
        }
    }
}
