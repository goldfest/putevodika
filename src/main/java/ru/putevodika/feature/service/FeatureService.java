package ru.putevodika.feature.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.feature.dto.FeatureResponse;
import ru.putevodika.feature.repository.FeatureRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeatureService {

    private final FeatureRepository featureRepository;

    public List<FeatureResponse> findAllActive() {
        return featureRepository
                .findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(feature ->
                        FeatureResponse.builder()
                                .id(feature.getId())
                                .code(feature.getCode())
                                .name(feature.getName())
                                .build()
                )
                .toList();
    }
}