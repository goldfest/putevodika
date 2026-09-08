package ru.putevodika.auth.exception;

public class InvalidPasswordResetTokenException
        extends RuntimeException {

    public InvalidPasswordResetTokenException() {
        super(
                "Ссылка восстановления недействительна или истекла"
        );
    }
}
