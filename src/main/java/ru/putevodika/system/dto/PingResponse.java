package ru.putevodika.system.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class PingResponse {

    String application;

    String status;

    Instant timestamp;
}