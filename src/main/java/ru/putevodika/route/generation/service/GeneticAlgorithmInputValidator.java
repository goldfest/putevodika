package ru.putevodika.route.generation.service;

import org.springframework.stereotype.Component;
import ru.putevodika.route.generation.dto.GeneticAlgorithmInput;
import ru.putevodika.route.generation.dto.GeneticAlgorithmPlace;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GeneticAlgorithmInputValidator {

    private static final Set<String> REQUIRED_FEATURES =
            Set.of(
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


    public void validate(
            GeneticAlgorithmInput input
    ) {

        validatePointIndexes(input);

        validateMatrix(
                input.durationsSeconds(),
                input.places().size() + 2,
                "durationsSeconds"
        );

        validateMatrix(
                input.distancesMeters(),
                input.places().size() + 2,
                "distancesMeters"
        );

        validateUserPreferences(input);

        validatePlaces(input);
    }


    private void validatePointIndexes(
            GeneticAlgorithmInput input
    ) {

        if (input.start().matrixIndex() != 0) {
            throw new IllegalStateException(
                    "START должен иметь matrixIndex = 0"
            );
        }

        for (int i = 0;
             i < input.places().size();
             i++) {

            GeneticAlgorithmPlace place =
                    input.places().get(i);

            int expectedIndex =
                    i + 1;

            if (place.matrixIndex()
                    != expectedIndex) {

                throw new IllegalStateException(
                        "Некорректный matrixIndex "
                                + "у place id="
                                + place.id()
                );
            }
        }

        int expectedFinishIndex =
                input.places().size() + 1;

        if (input.finish().matrixIndex()
                != expectedFinishIndex) {

            throw new IllegalStateException(
                    "Некорректный matrixIndex "
                            + "у FINISH"
            );
        }
    }


    private void validateMatrix(
            List<List<Double>> matrix,
            int expectedSize,
            String matrixName
    ) {

        if (matrix == null) {
            throw new IllegalStateException(
                    matrixName + " отсутствует"
            );
        }

        if (matrix.size() != expectedSize) {
            throw new IllegalStateException(
                    matrixName
                            + " имеет неправильное "
                            + "количество строк"
            );
        }

        for (List<Double> row : matrix) {

            if (row == null
                    || row.size()
                    != expectedSize) {

                throw new IllegalStateException(
                        matrixName
                                + " не является "
                                + "квадратной матрицей "
                                + expectedSize
                                + "x"
                                + expectedSize
                );
            }
        }
    }


    private void validateUserPreferences(
            GeneticAlgorithmInput input
    ) {

        if (!input.userPreferences()
                .keySet()
                .containsAll(
                        REQUIRED_FEATURES
                )) {

            throw new IllegalStateException(
                    "В userPreferences "
                            + "отсутствуют обязательные "
                            + "характеристики"
            );
        }

        input.userPreferences()
                .forEach(
                        (feature, value) -> {

                            if (value == null
                                    || value < 0.0
                                    || value > 1.0) {

                                throw new IllegalStateException(
                                        "Некорректное "
                                                + "предпочтение "
                                                + feature
                                );
                            }
                        }
                );
    }


    private void validatePlaces(
            GeneticAlgorithmInput input
    ) {

        Set<Long> uniqueIds =
                input.places()
                        .stream()
                        .map(
                                GeneticAlgorithmPlace::id
                        )
                        .collect(
                                Collectors.toSet()
                        );

        if (uniqueIds.size()
                != input.places().size()) {

            throw new IllegalStateException(
                    "В списке кандидатов "
                            + "есть повторяющиеся id"
            );
        }

        for (GeneticAlgorithmPlace place
                : input.places()) {

            if (place.visitDurationMinutes()
                    <= 0) {

                throw new IllegalStateException(
                        "Некорректная "
                                + "длительность посещения "
                                + "place id="
                                + place.id()
                );
            }

            if (!place.scores()
                    .keySet()
                    .containsAll(
                            REQUIRED_FEATURES
                    )) {

                throw new IllegalStateException(
                        "У place id="
                                + place.id()
                                + " отсутствуют scores"
                );
            }

            place.scores()
                    .forEach(
                            (feature, value) -> {

                                if (value == null
                                        || value < 0.0
                                        || value > 1.0) {

                                    throw new IllegalStateException(
                                            "Некорректный score "
                                                    + feature
                                                    + " у place id="
                                                    + place.id()
                                    );
                                }
                            }
                    );
        }
    }
}