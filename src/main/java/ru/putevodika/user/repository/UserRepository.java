package ru.putevodika.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.putevodika.user.entity.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
}