package ru.putevodika.auth.exception;

public class InvalidRefreshTokenException
        extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh-токен недействителен или истек");
    }
}