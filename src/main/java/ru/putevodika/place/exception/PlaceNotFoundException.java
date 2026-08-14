package ru.putevodika.place.exception;

public class PlaceNotFoundException extends RuntimeException {

    public PlaceNotFoundException(Long id) {
        super("Место с id %d не найдено".formatted(id));
    }
}