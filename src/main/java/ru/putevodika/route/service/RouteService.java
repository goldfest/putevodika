package ru.putevodika.route.service;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.common.dto.PageResponse;
import ru.putevodika.place.entity.Place;
import ru.putevodika.place.repository.PlaceRepository;
import ru.putevodika.route.dto.*;
import ru.putevodika.route.entity.RoutePlace;
import ru.putevodika.route.entity.RouteRating;
import ru.putevodika.route.entity.SavedRoute;
import ru.putevodika.route.exception.DuplicateRoutePlaceException;
import ru.putevodika.route.exception.RouteNotFoundException;
import ru.putevodika.route.exception.UnavailableRoutePlaceException;
import ru.putevodika.route.repository.RoutePlaceRepository;
import ru.putevodika.route.repository.RouteRatingRepository;
import ru.putevodika.route.repository.SavedRouteRepository;
import ru.putevodika.user.entity.UserAccount;
import ru.putevodika.user.exception.UserNotFoundException;
import ru.putevodika.user.repository.UserRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

    private final SavedRouteRepository
            routeRepository;

    private final RoutePlaceRepository
            routePlaceRepository;

    private final RouteRatingRepository
            routeRatingRepository;

    private final PlaceRepository
            placeRepository;

    private final UserRepository
            userRepository;

    private final GeometryFactory
            geometryFactory;


    @Transactional
    public RouteResponse create(
            Long userId,
            SaveRouteRequest request
    ) {
        UserAccount user =
                userRepository.findById(userId)
                        .orElseThrow(
                                () ->
                                        new UserNotFoundException(
                                                userId
                                        )
                        );

        validateNoDuplicates(
                request.getPlaceIds()
        );

        Map<Long, Place> places =
                resolvePlaces(
                        request.getPlaceIds()
                );

        Point startPoint =
                createPoint(
                        request.getStartLatitude(),
                        request.getStartLongitude()
                );

        Point finishPoint =
                createPoint(
                        request.getFinishLatitude(),
                        request.getFinishLongitude()
                );

        SavedRoute route =
                new SavedRoute(
                        user,
                        startPoint,
                        finishPoint
                );

        SavedRoute savedRoute =
                routeRepository.save(route);

        List<RoutePlace> routePlaces =
                IntStream.range(
                                0,
                                request.getPlaceIds().size()
                        )
                        .mapToObj(index -> {

                            Long placeId =
                                    request
                                            .getPlaceIds()
                                            .get(index);

                            return new RoutePlace(
                                    savedRoute,
                                    places.get(placeId),
                                    index + 1
                            );
                        })
                        .toList();

        routePlaceRepository.saveAll(
                routePlaces
        );

        return toResponse(savedRoute);
    }

    private void validateNoDuplicates(
            List<Long> placeIds
    ) {
        Set<Long> uniqueIds =
                new HashSet<>(placeIds);

        if (uniqueIds.size()
                != placeIds.size()) {

            throw new DuplicateRoutePlaceException();
        }
    }

    private Map<Long, Place> resolvePlaces(
            List<Long> placeIds
    ) {
        Set<Long> requestedIds =
                new HashSet<>(placeIds);

        List<Place> places =
                placeRepository
                        .findAllByIdInAndActiveTrueAndAvailableForRouteTrue(
                                requestedIds
                        );

        Set<Long> foundIds =
                places.stream()
                        .map(Place::getId)
                        .collect(
                                Collectors.toSet()
                        );

        Set<Long> unavailableIds =
                new TreeSet<>(requestedIds);

        unavailableIds.removeAll(foundIds);

        if (!unavailableIds.isEmpty()) {
            throw new UnavailableRoutePlaceException(
                    unavailableIds
            );
        }

        return places.stream()
                .collect(
                        Collectors.toMap(
                                Place::getId,
                                Function.identity()
                        )
                );
    }

    private Point createPoint(
            double latitude,
            double longitude
    ) {
        return geometryFactory.createPoint(
                new Coordinate(
                        longitude,
                        latitude
                )
        );
    }

    public RouteResponse getById(
            Long userId,
            Long routeId
    ) {
        return toResponse(
                getOwnedRoute(
                        userId,
                        routeId
                )
        );
    }

    private SavedRoute getOwnedRoute(
            Long userId,
            Long routeId
    ) {
        return routeRepository
                .findByIdAndUser_Id(
                        routeId,
                        userId
                )
                .orElseThrow(
                        () ->
                                new RouteNotFoundException(
                                        routeId
                                )
                );
    }

    public PageResponse<RouteListItemResponse>
    findAll(
            Long userId,
            int page,
            int size
    ) {
        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Page<RouteListItemResponse> result =
                routeRepository
                        .findAllByUser_Id(
                                userId,
                                pageable
                        )
                        .map(
                                this::toListItemResponse
                        );

        return PageResponse.from(result);
    }

    @Transactional
    public void delete(
            Long userId,
            Long routeId
    ) {
        SavedRoute route =
                getOwnedRoute(
                        userId,
                        routeId
                );

        routeRepository.delete(route);
    }

    @Transactional
    public RouteRatingResponse rate(
            Long userId,
            Long routeId,
            RateRouteRequest request
    ) {
        SavedRoute route =
                getOwnedRoute(
                        userId,
                        routeId
                );

        RouteRating rating =
                routeRatingRepository
                        .findByRoute_IdAndUser_Id(
                                routeId,
                                userId
                        )
                        .orElseGet(
                                () ->
                                        new RouteRating(
                                                route,
                                                route.getUser(),
                                                (short) request
                                                        .getRating()
                                        )
                        );

        rating.changeValue(
                (short) request.getRating()
        );

        RouteRating saved =
                routeRatingRepository.save(
                        rating
                );

        return RouteRatingResponse.builder()
                .routeId(routeId)
                .rating((int) saved.getValue())
                .build();
    }

    private RouteResponse toResponse(
            SavedRoute route
    ) {
        List<RoutePlaceResponse> places =
                routePlaceRepository
                        .findAllByRoute_IdOrderByPositionAsc(
                                route.getId()
                        )
                        .stream()
                        .map(this::toPlaceResponse)
                        .toList();

        Integer rating =
                routeRatingRepository
                        .findByRoute_IdAndUser_Id(
                                route.getId(),
                                route.getUser().getId()
                        )
                        .map(routeRating ->
                                (int) routeRating.getValue()
                        )
                        .orElse(null);

        return RouteResponse.builder()
                .id(route.getId())
                .start(
                        toPointResponse(
                                route.getStartPoint()
                        )
                )
                .finish(
                        toPointResponse(
                                route.getFinishPoint()
                        )
                )
                .walkingDistanceMeters(
                        route.getWalkingDistanceMeters()
                )
                .walkingDurationSeconds(
                        route.getWalkingDurationSeconds()
                )
                .totalDurationMinutes(
                        route.getTotalDurationMinutes()
                )
                .rating(rating)
                .places(places)
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .build();
    }

    private RoutePlaceResponse toPlaceResponse(
            RoutePlace routePlace
    ) {
        Place place =
                routePlace.getPlace();

        return RoutePlaceResponse.builder()
                .id(place.getId())
                .position(
                        routePlace.getPosition()
                )
                .name(place.getName())
                .address(place.getAddress())
                .latitude(
                        place.getLocation().getY()
                )
                .longitude(
                        place.getLocation().getX()
                )
                .build();
    }

    private RoutePointResponse toPointResponse(
            Point point
    ) {
        return RoutePointResponse.builder()
                .latitude(point.getY())
                .longitude(point.getX())
                .build();
    }

    private RouteListItemResponse
    toListItemResponse(
            SavedRoute route
    ) {
        return RouteListItemResponse.builder()
                .id(route.getId())
                .start(
                        toPointResponse(
                                route.getStartPoint()
                        )
                )
                .finish(
                        toPointResponse(
                                route.getFinishPoint()
                        )
                )
                .walkingDistanceMeters(
                        route.getWalkingDistanceMeters()
                )
                .walkingDurationSeconds(
                        route.getWalkingDurationSeconds()
                )
                .totalDurationMinutes(
                        route.getTotalDurationMinutes()
                )
                .createdAt(
                        route.getCreatedAt()
                )
                .build();
    }
}