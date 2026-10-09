package ru.putevodika.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.place.entity.Place;
import ru.putevodika.place.entity.PlaceSourceType;
import ru.putevodika.place.repository.PlaceRepository;
import ru.putevodika.route.repository.SavedRouteRepository;
import ru.putevodika.user.entity.UserRole;
import ru.putevodika.user.repository.UserRepository;

import static ru.putevodika.place.repository.specification.PlaceSpecifications.hasActive;
import static ru.putevodika.place.repository.specification.PlaceSpecifications.hasSourceType;
import static ru.putevodika.user.repository.specification.UserSpecifications.hasRole;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardStatsService {

    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final SavedRouteRepository savedRouteRepository;

    public Stats load() {
        Specification<Place> routeAvailable = (root, query, cb) ->
                cb.isTrue(root.get("availableForRoute"));

        return new Stats(
                userRepository.count(),
                userRepository.count(
                        ru.putevodika.user.repository.specification.UserSpecifications.hasActive(true)),
                userRepository.count(hasRole(UserRole.ADMIN)),
                placeRepository.count(),
                placeRepository.count(hasActive(true)),
                placeRepository.count(hasSourceType(PlaceSourceType.OSM)),
                placeRepository.count(Specification.allOf(hasActive(true), routeAvailable)),
                savedRouteRepository.count()
        );
    }

    public record Stats(
            long usersTotal,
            long usersActive,
            long administrators,
            long placesTotal,
            long placesActive,
            long osmPlaces,
            long routeAvailablePlaces,
            long savedRoutes
    ) {}
}
