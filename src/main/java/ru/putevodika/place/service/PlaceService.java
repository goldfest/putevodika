package ru.putevodika.place.service;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.place.dto.CreatePlaceRequest;
import ru.putevodika.place.dto.PlaceResponse;
import ru.putevodika.place.entity.Category;
import ru.putevodika.place.entity.Place;
import ru.putevodika.place.entity.PlaceSourceType;
import ru.putevodika.place.repository.CategoryRepository;
import ru.putevodika.place.repository.PlaceRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;

    private final CategoryRepository categoryRepository;

    private final GeometryFactory geometryFactory;


    @Transactional
    public PlaceResponse create(CreatePlaceRequest request) {

        Set<Category> categories =
                categoryRepository.findAllByCodeIn(
                        request.getCategories()
                );

        if (categories.size() != request.getCategories().size()) {
            throw new IllegalArgumentException(
                    "Unknown place category"
            );
        }

        Point location = geometryFactory.createPoint(
                new Coordinate(
                        request.getLongitude(),
                        request.getLatitude()
                )
        );

        Place place = new Place(
                request.getName(),
                request.getDescription(),
                request.getAddress(),
                location,
                PlaceSourceType.MANUAL,
                null
        );

        categories.forEach(place::addCategory);

        Place saved = placeRepository.save(place);

        return toResponse(saved);
    }


    public PlaceResponse getById(Long id) {

        Place place = placeRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Place not found: " + id
                        )
                );

        return toResponse(place);
    }


    private PlaceResponse toResponse(Place place) {

        Set<String> categories =
                place.getCategories()
                        .stream()
                        .map(Category::getCode)
                        .collect(Collectors.toSet());

        return PlaceResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .description(place.getDescription())
                .address(place.getAddress())
                .latitude(place.getLocation().getY())
                .longitude(place.getLocation().getX())
                .categories(categories)
                .sourceType(place.getSourceType().name())
                .active(place.isActive())
                .createdAt(place.getCreatedAt())
                .updatedAt(place.getUpdatedAt())
                .build();
    }
}