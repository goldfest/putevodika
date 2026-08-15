package ru.putevodika.route.exception;

public class RouteNotFoundException
        extends RuntimeException {

    public RouteNotFoundException(Long id) {
        super(
                "Маршрут с id "
                        + id
                        + " не найден"
        );
    }
}