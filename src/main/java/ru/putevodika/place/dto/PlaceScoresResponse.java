package ru.putevodika.place.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PlaceScoresResponse {

    BigDecimal nature;
    BigDecimal attractions;
    BigDecimal military;
    BigDecimal religion;
    BigDecimal architecture;
    BigDecimal history;
    BigDecimal art;
    BigDecimal souvenirs;

    @JsonProperty("transport_tech")
    BigDecimal transportTech;

    BigDecimal accommodation;
    BigDecimal food;

    @JsonProperty("exclusive_food")
    BigDecimal exclusiveFood;

    BigDecimal subcultures;
}