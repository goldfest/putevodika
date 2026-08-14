package ru.putevodika.place.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class PlaceListItemResponse {

    Long id;

    String name;

    String address;

    Double latitude;

    Double longitude;

    String sourceType;

    boolean active;

    Instant updatedAt;
}