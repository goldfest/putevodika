package ru.putevodika.user.exception;

public class UserNotFoundException
        extends RuntimeException {

    public UserNotFoundException(Long id) {
        super(
                "Пользователь с id %d не найден"
                        .formatted(id)
        );
    }
}