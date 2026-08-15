package ru.putevodika.feature.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.feature.dto.FeatureResponse;
import ru.putevodika.feature.repository.FeatureRepository;
import ru.putevodika.feature.entity.Feature;
import ru.putevodika.feature.exception.UnknownFeatureException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public Map<String, Feature> resolveActive(
            Set<String> requestedCodes
    ) {
        if (requestedCodes == null
                || requestedCodes.isEmpty()) {

            return Map.of();
        }

        Set<Feature> features =
                featureRepository
                        .findAllByCodeInAndActiveTrue(
                                requestedCodes
                        );

        Set<String> existingCodes =
                features.stream()
                        .map(Feature::getCode)
                        .collect(Collectors.toSet());

        Set<String> unknownCodes =
                new HashSet<>(requestedCodes);

        unknownCodes.removeAll(existingCodes);

        if (!unknownCodes.isEmpty()) {
            throw new UnknownFeatureException(
                    unknownCodes
            );
        }

        return features.stream()
                .collect(
                        Collectors.toMap(
                                Feature::getCode,
                                Function.identity()
                        )
                );
    }
}