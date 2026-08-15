package ru.putevodika.feature.exception;

import lombok.Getter;

import java.util.Set;
import java.util.TreeSet;

@Getter
public class UnknownFeatureException extends RuntimeException {

    private final Set<String> featureCodes;

    public UnknownFeatureException(
            Set<String> featureCodes
    ) {
        super(
                "Неизвестные характеристики: "
                        + String.join(
                        ", ",
                        new TreeSet<>(featureCodes)
                )
        );

        this.featureCodes = Set.copyOf(featureCodes);
    }
}