package ru.putevodika.route.exception;

import lombok.Getter;

@Getter
public class RouteTimeLimitExceededException
        extends RuntimeException {

    private final long availableDurationMinutes;

    private final long minimumRequiredDurationMinutes;


    public RouteTimeLimitExceededException(
            long availableDurationMinutes,
            long minimumRequiredDurationMinutes
    ) {
        super(
                "Маршрут не помещается "
                        + "в заданное временное ограничение"
        );

        this.availableDurationMinutes =
                availableDurationMinutes;

        this.minimumRequiredDurationMinutes =
                minimumRequiredDurationMinutes;
    }
}