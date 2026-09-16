package ru.putevodika.place.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.place.dto.CategoryResponse;
import ru.putevodika.place.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> findAllActive() {
        return categoryRepository
                .findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(category -> CategoryResponse.builder()
                        .id(category.getId())
                        .code(category.getCode())
                        .name(category.getName())
                        .build()
                )
                .toList();
    }

    public List<CategoryResponse> findAllPreferenceSelectable() {
        return categoryRepository
                .findAllByActiveTrueAndPreferenceSelectableTrueOrderByNameAsc()
                .stream()
                .map(category -> CategoryResponse.builder()
                        .id(category.getId())
                        .code(category.getCode())
                        .name(category.getName())
                        .build()
                )
                .toList();
    }
}