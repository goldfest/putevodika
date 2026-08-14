package ru.putevodika.place.dto;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class MapPlaceResponse {

    Long id;

    String name;

    Double latitude;

    Double longitude;

    Set<String> categories;
}