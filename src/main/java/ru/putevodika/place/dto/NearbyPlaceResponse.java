package ru.putevodika.place.dto;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class NearbyPlaceResponse {

    Long id;

    String name;

    String description;

    String address;

    Double latitude;

    Double longitude;

    Set<String> categories;

    String sourceType;

    long distanceMeters;
}