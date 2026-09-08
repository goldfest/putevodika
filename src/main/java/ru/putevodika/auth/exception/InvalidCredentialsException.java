package ru.putevodika.auth.exception;

public class InvalidCredentialsException
        extends RuntimeException {

    public InvalidCredentialsException() {
        super("Неверная почта или пароль");
    }
}
