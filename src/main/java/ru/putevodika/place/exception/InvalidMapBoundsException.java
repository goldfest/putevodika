package ru.putevodika.place.exception;

public class InvalidMapBoundsException extends RuntimeException {

    public InvalidMapBoundsException() {
        super(
                "Минимальные координаты области должны быть меньше максимальных"
        );
    }
}