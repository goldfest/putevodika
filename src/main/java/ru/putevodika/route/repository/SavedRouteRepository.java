package ru.putevodika.route.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.putevodika.route.entity.SavedRoute;

import java.util.Optional;

public interface SavedRouteRepository
        extends JpaRepository<SavedRoute, Long> {

    Page<SavedRoute> findAllByUser_Id(
            Long userId,
            Pageable pageable
    );

    Optional<SavedRoute> findByIdAndUser_Id(
            Long id,
            Long userId
    );
}