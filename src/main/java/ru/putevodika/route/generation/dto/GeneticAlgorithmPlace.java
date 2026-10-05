package ru.putevodika.route.generation.dto;

import java.util.Map;

public record GeneticAlgorithmPlace(

        Long id,

        int matrixIndex,

        double latitude,

        double longitude,

        int visitDurationMinutes,

        Map<String, Double> scores

) {
}