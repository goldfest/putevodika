package ru.putevodika.route.generation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.putevodika.place.entity.Place;
import ru.putevodika.place.entity.PlaceScore;
import ru.putevodika.place.repository.PlaceScoreRepository;
import ru.putevodika.route.dto.GenerateRouteRequest;
import ru.putevodika.route.generation.dto.GeneticAlgorithmInput;
import ru.putevodika.route.generation.dto.GeneticAlgorithmPlace;
import ru.putevodika.route.generation.dto.GeneticAlgorithmPoint;
import ru.putevodika.routing.dto.RoutingPointRequest;
import ru.putevodika.routing.dto.WalkingMatrixRequest;
import ru.putevodika.routing.dto.WalkingMatrixResponse;
import ru.putevodika.routing.service.RoutingService;
import ru.putevodika.user.service.UserFeaturePreferenceService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GeneticAlgorithmInputPreparationService {

    private final UserFeaturePreferenceService
            userFeaturePreferenceService;

    private final PlaceScoreRepository
            placeScoreRepository;

    private final RoutingService routingService;

    private final RouteCandidateSelectionService
            routeCandidateSelectionService;

    private final GeneticAlgorithmInputValidator
            inputValidator;

    private static final List<String> FEATURE_CODES =
            List.of(
                    "nature",
                    "attractions",
                    "military",
                    "religion",
                    "architecture",
                    "history",
                    "art",
                    "souvenirs",
                    "transport_tech",
                    "accommodation",
                    "food",
                    "exclusive_food",
                    "subcultures"
            );

    public GeneticAlgorithmInput prepare(
            Long userId,
            GenerateRouteRequest request
    ) {

        List<Place> candidatePlaces =
                routeCandidateSelectionService
                        .selectCandidates(request);

        return prepare(
                userId,
                request,
                candidatePlaces
        );
    }


    public GeneticAlgorithmInput prepare(
            Long userId,
            GenerateRouteRequest request,
            List<Place> candidatePlaces
    ) {

        Map<String, Double> userPreferences =
                normalizeUserPreferences(userId);

        Map<Long, PlaceScore> scoresByPlaceId =
                loadScores(candidatePlaces);

        /*
         * В алгоритм пока допускаем только объекты,
         * для которых есть:
         *
         * 1. длительность посещения;
         * 2. числовые scores.
         */
        List<Place> usablePlaces =
                candidatePlaces.stream()
                        .filter(place ->
                                place.getVisitDurationMinutes()
                                        != null
                        )
                        .filter(place ->
                                scoresByPlaceId.containsKey(
                                        place.getId()
                                )
                        )
                        .toList();

        List<RoutingPointRequest> matrixPoints =
                buildMatrixPoints(
                        request,
                        usablePlaces
                );

        WalkingMatrixResponse matrix =
                routingService.buildWalkingMatrix(
                        new WalkingMatrixRequest(
                                matrixPoints
                        )
                );

        List<GeneticAlgorithmPlace> places =
                IntStream.range(
                                0,
                                usablePlaces.size()
                        )
                        .mapToObj(index -> {

                            Place place =
                                    usablePlaces.get(index);

                            PlaceScore score =
                                    scoresByPlaceId.get(
                                            place.getId()
                                    );

                            return new GeneticAlgorithmPlace(
                                    place.getId(),

                                    /*
                                     * 0 = start,
                                     * поэтому первый объект = 1.
                                     */
                                    index + 1,

                                    place.getLocation().getY(),
                                    place.getLocation().getX(),

                                    place.getVisitDurationMinutes(),

                                    toScoresMap(score)
                            );
                        })
                        .toList();

        long availableDurationMinutes =
                Duration.between(
                                request.startTime(),
                                request.endTime()
                        )
                        .toMinutes();

        GeneticAlgorithmPoint start =
                new GeneticAlgorithmPoint(
                        request.start().latitude(),
                        request.start().longitude(),
                        0
                );

        GeneticAlgorithmPoint finish =
                new GeneticAlgorithmPoint(
                        request.finish().latitude(),
                        request.finish().longitude(),

                        /*
                         * start = 0
                         * places = 1..N
                         * finish = N + 1
                         */
                        places.size() + 1
                );

        GeneticAlgorithmInput input =
                new GeneticAlgorithmInput(
                        start,
                        finish,
                        availableDurationMinutes,
                        userPreferences,
                        places,
                        matrix.durationsSeconds(),
                        matrix.distancesMeters()
                );

        inputValidator.validate(input);

        return input;
    }


    private Map<String, Double>
    normalizeUserPreferences(
            Long userId
    ) {

        Map<String, Integer> preferences =
                userFeaturePreferenceService
                        .get(userId)
                        .getPreferences();

        Map<String, Double> normalized =
                new LinkedHashMap<>();

        FEATURE_CODES.forEach(feature -> {

            int weight =
                    preferences.getOrDefault(
                            feature,
                            0
                    );

            normalized.put(
                    feature,
                    weight / 5.0
            );
        });

        return normalized;
    }


    private Map<Long, PlaceScore> loadScores(
            List<Place> candidatePlaces
    ) {

        List<Long> placeIds =
                candidatePlaces.stream()
                        .map(Place::getId)
                        .toList();

        return placeScoreRepository
                .findAllById(placeIds)
                .stream()
                .collect(
                        Collectors.toMap(
                                PlaceScore::getPlaceId,
                                Function.identity()
                        )
                );
    }


    private List<RoutingPointRequest>
    buildMatrixPoints(
            GenerateRouteRequest request,
            List<Place> places
    ) {

        List<RoutingPointRequest> points =
                new ArrayList<>();

        /*
         * Индекс 0.
         */
        points.add(
                new RoutingPointRequest(
                        request.start().latitude(),
                        request.start().longitude()
                )
        );

        /*
         * Индексы 1..N.
         */
        places.forEach(place ->
                points.add(
                        new RoutingPointRequest(
                                place.getLocation().getY(),
                                place.getLocation().getX()
                        )
                )
        );

        /*
         * Последний индекс N + 1.
         */
        points.add(
                new RoutingPointRequest(
                        request.finish().latitude(),
                        request.finish().longitude()
                )
        );

        return points;
    }


    private Map<String, Double> toScoresMap(
            PlaceScore score
    ) {

        Map<String, Double> scores =
                new LinkedHashMap<>();

        scores.put(
                "nature",
                score.getNature().doubleValue()
        );

        scores.put(
                "attractions",
                score.getAttractions().doubleValue()
        );

        scores.put(
                "military",
                score.getMilitary().doubleValue()
        );

        scores.put(
                "religion",
                score.getReligion().doubleValue()
        );

        scores.put(
                "architecture",
                score.getArchitecture().doubleValue()
        );

        scores.put(
                "history",
                score.getHistory().doubleValue()
        );

        scores.put(
                "art",
                score.getArt().doubleValue()
        );

        scores.put(
                "souvenirs",
                score.getSouvenirs().doubleValue()
        );

        scores.put(
                "transport_tech",
                score.getTransportTech().doubleValue()
        );

        scores.put(
                "accommodation",
                score.getAccommodation().doubleValue()
        );

        scores.put(
                "food",
                score.getFood().doubleValue()
        );

        scores.put(
                "exclusive_food",
                score.getExclusiveFood().doubleValue()
        );

        scores.put(
                "subcultures",
                score.getSubcultures().doubleValue()
        );

        return scores;
    }
}