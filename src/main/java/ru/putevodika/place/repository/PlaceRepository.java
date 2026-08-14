package ru.putevodika.place.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import ru.putevodika.place.entity.Place;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PlaceRepository
        extends JpaRepository<Place, Long>,
        JpaSpecificationExecutor<Place> {

    @Override
    @EntityGraph(attributePaths = "categories")
    Optional<Place> findById(Long id);

    @NativeQuery("""
            SELECT p.*
            FROM place p
            WHERE p.active = TRUE
              AND ST_DWithin(
                    p.location::geography,
                    ST_SetSRID(
                        ST_MakePoint(:longitude, :latitude),
                        4326
                    )::geography,
                    :radiusMeters
              )
            ORDER BY ST_Distance(
                p.location::geography,
                ST_SetSRID(
                    ST_MakePoint(:longitude, :latitude),
                    4326
                )::geography
            )
            LIMIT :limit
            """)
    List<Place> findActiveNearby(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusMeters") int radiusMeters,
            @Param("limit") int limit
    );
    @NativeQuery("""
        SELECT p.*
        FROM place p
        WHERE p.active = TRUE
          AND ST_DWithin(
                p.location::geography,
                ST_SetSRID(
                    ST_MakePoint(:longitude, :latitude),
                    4326
                )::geography,
                :radiusMeters
          )
          AND EXISTS (
                SELECT 1
                FROM place_category pc
                JOIN category c
                  ON c.id = pc.category_id
                WHERE pc.place_id = p.id
                  AND c.active = TRUE
                  AND c.code IN (:categoryCodes)
          )
        ORDER BY ST_Distance(
            p.location::geography,
            ST_SetSRID(
                ST_MakePoint(:longitude, :latitude),
                4326
            )::geography
        )
        LIMIT :limit
        """)
    List<Place> findActiveNearbyByCategories(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusMeters") int radiusMeters,
            @Param("categoryCodes") Set<String> categoryCodes,
            @Param("limit") int limit
    );

    @NativeQuery("""
        SELECT p.*
        FROM place p
        WHERE p.active = TRUE
          AND ST_Intersects(
                p.location,
                ST_MakeEnvelope(
                    :minLongitude,
                    :minLatitude,
                    :maxLongitude,
                    :maxLatitude,
                    4326
                )
          )
        ORDER BY p.id
        LIMIT :limit
        """)
    List<Place> findActiveInBounds(
            @Param("minLatitude") double minLatitude,
            @Param("minLongitude") double minLongitude,
            @Param("maxLatitude") double maxLatitude,
            @Param("maxLongitude") double maxLongitude,
            @Param("limit") int limit
    );

    @NativeQuery("""
        SELECT p.*
        FROM place p
        WHERE p.active = TRUE
          AND ST_Intersects(
                p.location,
                ST_MakeEnvelope(
                    :minLongitude,
                    :minLatitude,
                    :maxLongitude,
                    :maxLatitude,
                    4326
                )
          )
          AND EXISTS (
                SELECT 1
                FROM place_category pc
                JOIN category c
                  ON c.id = pc.category_id
                WHERE pc.place_id = p.id
                  AND c.active = TRUE
                  AND c.code IN (:categoryCodes)
          )
        ORDER BY p.id
        LIMIT :limit
        """)
    List<Place> findActiveInBoundsByCategories(
            @Param("minLatitude") double minLatitude,
            @Param("minLongitude") double minLongitude,
            @Param("maxLatitude") double maxLatitude,
            @Param("maxLongitude") double maxLongitude,
            @Param("categoryCodes") Set<String> categoryCodes,
            @Param("limit") int limit
    );
}