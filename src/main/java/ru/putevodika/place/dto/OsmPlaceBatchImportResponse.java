package ru.putevodika.place.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class OsmPlaceBatchImportResponse {

    int total;

    int created;

    int updated;

    int failed;

    List<Failure> failures;

    @Value
    @Builder
    public static class Failure {

        String id;

        String error;
    }
}