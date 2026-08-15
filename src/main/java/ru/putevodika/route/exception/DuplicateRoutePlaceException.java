package ru.putevodika.route.exception;

public class DuplicateRoutePlaceException
        extends RuntimeException {

    public DuplicateRoutePlaceException() {
        super(
                "Маршрут не может содержать "
                        + "одну достопримечательность несколько раз"
        );
    }
}