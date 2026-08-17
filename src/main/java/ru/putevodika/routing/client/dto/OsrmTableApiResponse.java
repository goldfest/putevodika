package ru.putevodika.routing.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OsrmTableApiResponse(

        String code,

        String message,

        List<List<Double>> durations,

        List<List<Double>> distances

) {
}