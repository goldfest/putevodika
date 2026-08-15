package ru.putevodika.feature.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.putevodika.feature.entity.PlaceFeature;

import java.util.List;

public interface PlaceFeatureRepository
        extends JpaRepository<PlaceFeature, Long> {

    @Query("""
            select pf
            from PlaceFeature pf
            join fetch pf.feature
            where pf.place.id = :placeId
            """)
    List<PlaceFeature> findAllByPlaceId(
            @Param("placeId") Long placeId
    );

    @Modifying
    @Query("""
            delete from PlaceFeature pf
            where pf.place.id = :placeId
            """)
    void deleteAllByPlaceId(
            @Param("placeId") Long placeId
    );
}