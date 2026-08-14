package ru.putevodika.user.exception;

public class EmailAlreadyUsedException
        extends RuntimeException {

    public EmailAlreadyUsedException(String email) {
        super(
                "Пользователь с email %s уже существует"
                        .formatted(email)
        );
    }
}