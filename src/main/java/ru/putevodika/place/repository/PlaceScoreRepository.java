package ru.putevodika.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.putevodika.place.entity.PlaceScore;

public interface PlaceScoreRepository
        extends JpaRepository<PlaceScore, Long> {
}