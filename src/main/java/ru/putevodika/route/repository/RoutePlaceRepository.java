package ru.putevodika.route.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.putevodika.route.entity.RoutePlace;

import java.util.List;

public interface RoutePlaceRepository
        extends JpaRepository<RoutePlace, Long> {

    @EntityGraph(attributePaths = "place")
    List<RoutePlace>
    findAllByRoute_IdOrderByPositionAsc(
            Long routeId
    );
}