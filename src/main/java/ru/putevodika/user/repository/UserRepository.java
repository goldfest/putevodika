package ru.putevodika.user.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.putevodika.user.entity.UserAccount;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<UserAccount, Long>,
        JpaSpecificationExecutor<UserAccount> {

    Optional<UserAccount> findByEmailIgnoreCase(
            String email
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    @Override
    @EntityGraph(attributePaths = "preferredCategories")
    Optional<UserAccount> findById(Long id);

    boolean existsByIdAndActiveTrue(Long id);

    @Query("""
            select user.authVersion
            from UserAccount user
            where user.id = :userId
              and user.active = true
            """)
    Optional<Long> findAuthVersionByIdAndActiveTrue(
            @Param("userId") Long userId
    );
}
