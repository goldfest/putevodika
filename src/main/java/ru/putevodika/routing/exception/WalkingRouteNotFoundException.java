package ru.putevodika.routing.exception;

public class WalkingRouteNotFoundException
        extends RuntimeException {

    public WalkingRouteNotFoundException(
            String message
    ) {
        super(message);
    }
}