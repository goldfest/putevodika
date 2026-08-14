package ru.putevodika.place.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.putevodika.place.entity.Place;
import ru.putevodika.place.entity.PlaceSourceType;

import java.util.Locale;

public final class PlaceSpecifications {

    private PlaceSpecifications() {
    }


    public static Specification<Place> hasActive(
            Boolean active
    ) {
        return (root, query, builder) -> {

            if (active == null) {
                return builder.conjunction();
            }

            return builder.equal(
                    root.get("active"),
                    active
            );
        };
    }


    public static Specification<Place> hasSourceType(
            PlaceSourceType sourceType
    ) {
        return (root, query, builder) -> {

            if (sourceType == null) {
                return builder.conjunction();
            }

            return builder.equal(
                    root.get("sourceType"),
                    sourceType
            );
        };
    }


    public static Specification<Place> hasCategory(
            String categoryCode
    ) {
        return (root, query, builder) -> {

            if (categoryCode == null
                    || categoryCode.isBlank()) {

                return builder.conjunction();
            }

            String normalizedCode =
                    categoryCode
                            .trim()
                            .toUpperCase(Locale.ROOT);

            return builder.equal(
                    root.join("categories")
                            .get("code"),
                    normalizedCode
            );
        };
    }


    public static Specification<Place> nameContains(
            String search
    ) {
        return (root, query, builder) -> {

            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }

            String normalizedSearch =
                    search
                            .trim()
                            .toLowerCase(Locale.ROOT);

            return builder.like(
                    builder.lower(
                            root.get("name")
                    ),
                    "%" + normalizedSearch + "%"
            );
        };
    }
}