package ru.putevodika.user.exception;

import java.util.Set;

public class UnknownPreferenceCategoryException
        extends RuntimeException {

    private final Set<String> categoryCodes;

    public UnknownPreferenceCategoryException(
            Set<String> categoryCodes
    ) {
        super(
                "Неизвестные категории предпочтений: "
                        + String.join(", ", categoryCodes)
        );

        this.categoryCodes =
                Set.copyOf(categoryCodes);
    }

    public Set<String> getCategoryCodes() {
        return categoryCodes;
    }
}