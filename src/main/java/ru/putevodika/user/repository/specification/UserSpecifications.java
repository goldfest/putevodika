package ru.putevodika.user.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.putevodika.user.entity.UserAccount;
import ru.putevodika.user.entity.UserRole;

import java.util.Locale;

public final class UserSpecifications {

    private UserSpecifications() {
    }


    public static Specification<UserAccount> hasActive(
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


    public static Specification<UserAccount> hasRole(
            UserRole role
    ) {
        return (root, query, builder) -> {

            if (role == null) {
                return builder.conjunction();
            }

            return builder.equal(
                    root.get("role"),
                    role
            );
        };
    }


    public static Specification<UserAccount> containsSearch(
            String search
    ) {
        return (root, query, builder) -> {

            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }

            String pattern =
                    "%"
                            + search.trim()
                            .toLowerCase(Locale.ROOT)
                            + "%";

            return builder.or(
                    builder.like(
                            builder.lower(
                                    root.get("email")
                            ),
                            pattern
                    ),
                    builder.like(
                            builder.lower(
                                    root.get("displayName")
                            ),
                            pattern
                    )
            );
        };
    }
}