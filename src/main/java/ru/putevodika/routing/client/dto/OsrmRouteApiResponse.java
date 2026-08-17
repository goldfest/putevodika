package ru.putevodika.routing.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OsrmRouteApiResponse(

        String code,

        String message,

        List<Route> routes

) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Route(

            double distance,

            double duration,

            Geometry geometry

    ) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Geometry(

            String type,

            List<List<Double>> coordinates

    ) {
    }
}