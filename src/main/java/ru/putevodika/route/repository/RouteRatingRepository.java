package ru.putevodika.route.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.putevodika.route.entity.RouteRating;

import java.util.Optional;

public interface RouteRatingRepository
        extends JpaRepository<RouteRating, Long> {

    Optional<RouteRating> findByRoute_IdAndUser_Id(
            Long routeId,
            Long userId
    );
}