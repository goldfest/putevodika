package ru.putevodika.routing.client;

import java.util.List;

public record OsrmMatrixResult(

        List<List<Double>> durationsSeconds,

        List<List<Double>> distancesMeters

) {
}