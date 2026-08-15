package ru.putevodika.route.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RoutePlaceResponse {

    Long id;

    int position;

    String name;

    String address;

    Double latitude;

    Double longitude;
}