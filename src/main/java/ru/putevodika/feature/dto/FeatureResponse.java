package ru.putevodika.feature.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FeatureResponse {

    Long id;

    String code;

    String name;
}