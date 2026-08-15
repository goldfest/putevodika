package ru.putevodika.route.exception;

import lombok.Getter;

import java.util.Set;
import java.util.TreeSet;

@Getter
public class UnavailableRoutePlaceException
        extends RuntimeException {

    private final Set<Long> placeIds;

    public UnavailableRoutePlaceException(
            Set<Long> placeIds
    ) {
        super(
                "Достопримечательности не найдены "
                        + "или отключены: "
                        + new TreeSet<>(placeIds)
        );

        this.placeIds =
                Set.copyOf(placeIds);
    }
}