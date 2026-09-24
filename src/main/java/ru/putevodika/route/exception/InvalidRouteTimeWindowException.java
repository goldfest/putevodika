package ru.putevodika.route.exception;

public class InvalidRouteTimeWindowException
        extends RuntimeException {

    public InvalidRouteTimeWindowException() {
        super(
                "Время окончания маршрута "
                        + "должно быть позже времени начала"
        );
    }
}