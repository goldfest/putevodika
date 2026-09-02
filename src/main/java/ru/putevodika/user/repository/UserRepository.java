package ru.putevodika.user.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.putevodika.user.entity.UserAccount;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<UserAccount, Long>,
        JpaSpecificationExecutor<UserAccount> {

    Optional<UserAccount> findByLoginIgnoreCase(
            String login
    );

    boolean existsByLoginIgnoreCase(
            String login
    );

    @Override
    @EntityGraph(attributePaths = "preferredCategories")
    Optional<UserAccount> findById(Long id);

    boolean existsByIdAndActiveTrue(Long id);
}