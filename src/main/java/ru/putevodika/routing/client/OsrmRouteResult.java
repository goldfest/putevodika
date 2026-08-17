package ru.putevodika.routing.client;

import java.util.List;

public record OsrmRouteResult(

        double distanceMeters,

        double durationSeconds,

        String geometryType,

        List<List<Double>> coordinates

) {
}