package ru.putevodika.feature.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.putevodika.feature.entity.UserFeaturePreference;

import java.util.List;

public interface UserFeaturePreferenceRepository
        extends JpaRepository<UserFeaturePreference, Long> {

    @Query("""
            select p
            from UserFeaturePreference p
            join fetch p.feature
            where p.user.id = :userId
            """)
    List<UserFeaturePreference> findAllByUserId(
            @Param("userId") Long userId
    );

    @Modifying
    @Query("""
            delete from UserFeaturePreference p
            where p.user.id = :userId
            """)
    void deleteAllByUserId(
            @Param("userId") Long userId
    );
}