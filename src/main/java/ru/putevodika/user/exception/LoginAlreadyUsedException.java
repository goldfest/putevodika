package ru.putevodika.user.exception;

public class LoginAlreadyUsedException
        extends RuntimeException {

    public LoginAlreadyUsedException(String login) {
        super(
                "Пользователь с логином %s уже существует"
                        .formatted(login)
        );
    }
}