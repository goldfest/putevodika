package ru.putevodika.routing.dto;

import java.util.List;

public record WalkingMatrixResponse(

        List<List<Double>> durationsSeconds,

        List<List<Double>> distancesMeters

) {
}