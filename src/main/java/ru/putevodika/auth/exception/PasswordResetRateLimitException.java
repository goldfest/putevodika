package ru.putevodika.auth.exception;

public class PasswordResetRateLimitException
        extends RuntimeException {

    public PasswordResetRateLimitException() {
        super(
                "Слишком много запросов восстановления. Попробуйте позже"
        );
    }
}
