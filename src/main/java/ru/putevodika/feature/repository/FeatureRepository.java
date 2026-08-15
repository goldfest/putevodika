package ru.putevodika.feature.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.putevodika.feature.entity.Feature;

import java.util.List;
import java.util.Set;

public interface FeatureRepository
        extends JpaRepository<Feature, Long> {

    List<Feature> findAllByActiveTrueOrderByNameAsc();

    Set<Feature> findAllByCodeInAndActiveTrue(
            Set<String> codes
    );
}