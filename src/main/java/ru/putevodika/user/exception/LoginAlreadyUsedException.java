package ru.putevodika.user.exception;

public class LoginAlreadyUsedException
        extends RuntimeException {

    public LoginAlreadyUsedException(String email) {
        super(
                "Пользователь с почтой %s уже существует"
                        .formatted(email)
        );
    }
}
