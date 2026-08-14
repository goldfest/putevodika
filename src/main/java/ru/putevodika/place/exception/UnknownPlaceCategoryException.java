package ru.putevodika.place.exception;

import java.util.Set;

public class UnknownPlaceCategoryException extends RuntimeException {

    private final Set<String> categoryCodes;

    public UnknownPlaceCategoryException(Set<String> categoryCodes) {
        super("Неизвестные категории: " + String.join(", ", categoryCodes));
        this.categoryCodes = Set.copyOf(categoryCodes);
    }

    public Set<String> getCategoryCodes() {
        return categoryCodes;
    }
}