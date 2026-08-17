package ru.putevodika.routing.exception;

public class RoutingProviderUnavailableException
        extends RuntimeException {

    public RoutingProviderUnavailableException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }

    public RoutingProviderUnavailableException(
            String message
    ) {
        super(message);
    }
}