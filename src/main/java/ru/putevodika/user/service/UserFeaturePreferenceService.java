package ru.putevodika.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.feature.entity.Feature;
import ru.putevodika.feature.entity.UserFeaturePreference;
import ru.putevodika.feature.repository.UserFeaturePreferenceRepository;
import ru.putevodika.feature.service.FeatureService;
import ru.putevodika.user.dto.FeaturePreferencesResponse;
import ru.putevodika.user.dto.UpdateFeaturePreferencesRequest;
import ru.putevodika.user.entity.UserAccount;
import ru.putevodika.user.exception.UserNotFoundException;
import ru.putevodika.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFeaturePreferenceService {

    private final UserRepository userRepository;

    private final UserFeaturePreferenceRepository
            preferenceRepository;

    private final FeatureService featureService;


    public FeaturePreferencesResponse get(
            Long userId
    ) {
        UserAccount user = getUser(userId);

        List<UserFeaturePreference> preferences =
                preferenceRepository
                        .findAllByUserId(
                                user.getId()
                        );

        return toResponse(preferences);
    }


    @Transactional
    public FeaturePreferencesResponse replace(
            Long userId,
            UpdateFeaturePreferencesRequest request
    ) {
        UserAccount user = getUser(userId);

        Map<String, Integer> requested =
                request.getPreferences();

        Map<String, Feature> features =
                featureService.resolveActive(
                        requested.keySet()
                );

        preferenceRepository.deleteAllByUserId(
                userId
        );

        List<UserFeaturePreference> preferences =
                requested.entrySet()
                        .stream()
                        .map(entry ->
                                new UserFeaturePreference(
                                        user,
                                        features.get(
                                                entry.getKey()
                                        ),
                                        entry.getValue()
                                                .shortValue()
                                )
                        )
                        .toList();

        preferenceRepository.saveAll(
                preferences
        );

        return toResponse(preferences);
    }


    private FeaturePreferencesResponse toResponse(
            List<UserFeaturePreference> preferences
    ) {
        Map<String, Integer> values =
                preferences.stream()
                        .collect(
                                Collectors.toMap(
                                        preference ->
                                                preference
                                                        .getFeature()
                                                        .getCode(),
                                        preference ->
                                                (int) preference
                                                        .getWeight()
                                )
                        );

        return FeaturePreferencesResponse
                .builder()
                .preferences(values)
                .build();
    }


    private UserAccount getUser(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new UserNotFoundException(
                                        id
                                )
                );
    }
}