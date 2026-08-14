package ru.putevodika.place.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Set;

@Value
@Builder
public class PlaceResponse {

    Long id;

    String name;

    String description;

    String address;

    Double latitude;

    Double longitude;

    Set<String> categories;

    String sourceType;

    boolean active;

    Instant createdAt;

    Instant updatedAt;
}