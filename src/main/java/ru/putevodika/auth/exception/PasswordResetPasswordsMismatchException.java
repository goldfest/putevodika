package ru.putevodika.auth.exception;

public class PasswordResetPasswordsMismatchException
        extends RuntimeException {

    public PasswordResetPasswordsMismatchException() {
        super("Новый пароль и подтверждение не совпадают");
    }
}
