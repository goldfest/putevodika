package ru.putevodika.place.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.putevodika.place.dto.CategoryResponse;
import ru.putevodika.place.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(
        name = "Категории",
        description = "Справочник категорий туристических объектов"
)
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryService.findAllActive();
    }

    @GetMapping("/preferences")
    public List<CategoryResponse> findPreferences() {
        return categoryService.findAllPreferenceSelectable();
    }
}