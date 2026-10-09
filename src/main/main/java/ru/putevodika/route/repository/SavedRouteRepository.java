package ru.putevodika.route.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.putevodika.route.entity.SavedRoute;

import java.util.Optional;

public interface SavedRouteRepository
        extends JpaRepository<SavedRoute, Long>, JpaSpecificationExecutor<SavedRoute> {

    // Existing user-facing queries are preserved.
    Page<SavedRoute> findAllByUser_Id(Long userId, Pageable pageable);

    Optional<SavedRoute> findByIdAndUser_Id(Long id, Long userId);

    // Fetch only to-one relation: safe with paginated results.
    @Override
    @EntityGraph(attributePaths = "user")
    Page<SavedRoute> findAll(Specification<SavedRoute> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<SavedRoute> findById(Long id);
}
