package ru.putevodika.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.putevodika.place.entity.Category;

import java.util.List;
import java.util.Set;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    Set<Category> findAllByCodeInAndActiveTrue(
            Set<String> codes
    );

    List<Category> findAllByActiveTrueOrderByNameAsc();

    List<Category>
    findAllByActiveTrueAndPreferenceSelectableTrueOrderByNameAsc();

    Set<Category>
    findAllByCodeInAndActiveTrueAndPreferenceSelectableTrue(
            Set<String> codes
    );
}