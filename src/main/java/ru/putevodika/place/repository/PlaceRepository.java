package ru.putevodika.place.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.putevodika.place.entity.Place;

import java.util.Optional;

public interface PlaceRepository
        extends JpaRepository<Place, Long> {

    @Override
    @EntityGraph(attributePaths = "categories")
    Optional<Place> findById(Long id);
}