package ru.putevodika.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.putevodika.place.entity.Category;

import java.util.Set;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    Set<Category> findAllByCodeIn(Set<String> codes);
}