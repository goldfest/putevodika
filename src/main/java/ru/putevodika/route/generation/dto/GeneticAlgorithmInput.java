package ru.putevodika.route.generation.dto;

import java.util.List;
import java.util.Map;

public record GeneticAlgorithmInput(

        GeneticAlgorithmPoint start,

        GeneticAlgorithmPoint finish,

        long availableDurationMinutes,

        Map<String, Double> userPreferences,

        List<GeneticAlgorithmPlace> places,

        List<List<Double>> durationsSeconds,

        List<List<Double>> distancesMeters

) {
}