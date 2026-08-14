package ru.putevodika.user.exception;

public class IncorrectCurrentPasswordException
        extends RuntimeException {

    public IncorrectCurrentPasswordException() {
        super("Текущий пароль указан неверно");
    }
}