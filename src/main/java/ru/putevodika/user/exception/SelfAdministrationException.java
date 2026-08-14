package ru.putevodika.user.exception;

public class SelfAdministrationException
        extends RuntimeException {

    public SelfAdministrationException() {
        super(
                "Нельзя изменить роль или активность собственной учетной записи через административный API"
        );
    }
}