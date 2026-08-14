package ru.putevodika.auth.exception;

public class UserInactiveException
        extends RuntimeException {

    public UserInactiveException() {
        super("Учетная запись пользователя отключена");
    }
}