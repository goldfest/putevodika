package ru.putevodika.feature.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.putevodika.feature.dto.FeatureResponse;
import ru.putevodika.feature.service.FeatureService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/features")
@RequiredArgsConstructor
@Tag(
        name = "Характеристики",
        description = "Справочник числовых характеристик для персонализации маршрутов"
)
public class FeatureController {

    private final FeatureService featureService;

    @GetMapping
    public List<FeatureResponse> findAll() {
        return featureService.findAllActive();
    }
}