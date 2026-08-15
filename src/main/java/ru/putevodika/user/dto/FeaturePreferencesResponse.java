package ru.putevodika.user.dto;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class FeaturePreferencesResponse {

    Map<String, Integer> preferences;
}