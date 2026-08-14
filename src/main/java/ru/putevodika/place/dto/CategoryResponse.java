package ru.putevodika.place.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CategoryResponse {

    Long id;

    String code;

    String name;
}