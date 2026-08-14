package ru.putevodika.system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.putevodika.system.dto.PingResponse;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
public class SystemController {

    @GetMapping("/ping")
    public PingResponse ping() {

        return PingResponse.builder()
                .application("putevodika")
                .status("ok")
                .timestamp(Instant.now())
                .build();
    }
}