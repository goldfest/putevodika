package ru.putevodika.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.admin.dto.AdminRouteViews;
import ru.putevodika.common.dto.PageResponse;
import ru.putevodika.place.entity.Place;
import ru.putevodika.route.entity.RoutePlace;
import ru.putevodika.route.entity.SavedRoute;
import ru.putevodika.route.exception.RouteNotFoundException;
import ru.putevodika.route.repository.RoutePlaceRepository;
import ru.putevodika.route.repository.RouteRatingRepository;
import ru.putevodika.route.repository.SavedRouteRepository;
import ru.putevodika.user.entity.UserAccount;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Administrative, read-only access across all owners; does not modify routes. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRouteService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm 'UTC'")
                    .withZone(ZoneOffset.UTC);

    private final SavedRouteRepository routeRepository;
    private final RoutePlaceRepository routePlaceRepository;
    private final RouteRatingRepository routeRatingRepository;

    public PageResponse<AdminRouteViews.RouteRow> findAll(
            int page, int size, String search, Long userId,
            LocalDate dateFrom, LocalDate dateTo
    ) {
        List<Specification<SavedRoute>> filters = new ArrayList<>();

        if (userId != null) {
            filters.add((root, query, cb) ->
                    cb.equal(root.get("user").get("id"), userId));
        }

        if (dateFrom != null) {
            Instant lower = dateFrom.atStartOfDay().toInstant(ZoneOffset.UTC);
            filters.add((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), lower));
        }
        if (dateTo != null) {
            // Upper bound is exclusive: includes the entire selected UTC day.
            Instant upper = dateTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            filters.add((root, query, cb) ->
                    cb.lessThan(root.get("createdAt"), upper));
        }

        String cleanSearch = search == null ? "" : search.strip();
        if (!cleanSearch.isEmpty()) {
            String escaped = cleanSearch.toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            String pattern = "%" + escaped + "%";
            Long numericId = null;
            try {
                numericId = Long.parseLong(cleanSearch);
            } catch (NumberFormatException ignored) {
                // Non-numeric text is searched only in the owner's name and email.
            }
            Long exactId = numericId;
            filters.add((root, query, cb) -> {
                Join<SavedRoute, UserAccount> user = root.join("user", JoinType.INNER);
                Predicate name = cb.like(cb.lower(user.get("displayName")), pattern, '\\');
                Predicate email = cb.like(cb.lower(user.get("email")), pattern, '\\');
                if (exactId != null) {
                    return cb.or(name, email, cb.equal(root.get("id"), exactId));
                }
                return cb.or(name, email);
            });
        }

        Specification<SavedRoute> specification = Specification.allOf(filters);
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        Page<SavedRoute> result = routeRepository.findAll(specification, pageable);

        Set<Long> routeIds = result.getContent().stream()
                .map(SavedRoute::getId).collect(Collectors.toSet());
        Map<Long, Long> stopCounts = loadStopCounts(routeIds);

        Page<AdminRouteViews.RouteRow> rows = result.map(route -> {
            UserAccount user = route.getUser();
            return new AdminRouteViews.RouteRow(
                    route.getId(), user.getId(), user.getDisplayName(), user.getEmail(),
                    stopCounts.getOrDefault(route.getId(), 0L),
                    formatMeters(route.getWalkingDistanceMeters()),
                    formatMinutes(route.getTotalDurationMinutes()),
                    formatDate(route.getCreatedAt())
            );
        });
        return PageResponse.from(rows);
    }

    public AdminRouteViews.RouteDetail getById(Long routeId) {
        SavedRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));
        UserAccount user = route.getUser();
        List<AdminRouteViews.RouteStop> stops = routePlaceRepository
                .findAllByRoute_IdOrderByPositionAsc(routeId).stream()
                .map(this::toStop).toList();
        Integer rating = routeRatingRepository
                .findByRoute_IdAndUser_Id(routeId, user.getId())
                .map(value -> (int) value.getValue()).orElse(null);
        return new AdminRouteViews.RouteDetail(
                route.getId(), user.getId(), user.getDisplayName(), user.getEmail(),
                new AdminRouteViews.GeoPoint(route.getStartPoint().getY(), route.getStartPoint().getX()),
                new AdminRouteViews.GeoPoint(route.getFinishPoint().getY(), route.getFinishPoint().getX()),
                formatMeters(route.getWalkingDistanceMeters()),
                formatSeconds(route.getWalkingDurationSeconds()),
                formatMinutes(route.getTotalDurationMinutes()),
                rating, formatDate(route.getCreatedAt()), formatDate(route.getUpdatedAt()),
                stops
        );
    }

    private AdminRouteViews.RouteStop toStop(RoutePlace routePlace) {
        Place place = routePlace.getPlace();
        return new AdminRouteViews.RouteStop(
                routePlace.getPosition(), place.getId(), place.getName(),
                place.getAddress(), place.getLocation().getY(), place.getLocation().getX(),
                place.isActive(), place.isAvailableForRoute()
        );
    }

    private Map<Long, Long> loadStopCounts(Collection<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        Map<Long, Long> counts = new HashMap<>();
        for (RoutePlaceRepository.RouteStopCount result :
                routePlaceRepository.countStopsForRoutes(ids)) {
            counts.put(result.getRouteId(), result.getStopCount());
        }
        return counts;
    }

    private static String formatDate(Instant date) {
        return date == null ? "—" : DATE_FORMAT.format(date);
    }

    private static String formatMeters(Integer meters) {
        if (meters == null) return "—";
        return String.format(Locale.forLanguageTag("ru-RU"), "%.2f км", meters / 1000.0);
    }

    private static String formatMinutes(Integer minutes) {
        return minutes == null ? "—" : minutes + " мин";
    }

    private static String formatSeconds(Integer seconds) {
        if (seconds == null) return "—";
        return String.format("%d ч %02d мин", seconds / 3600, (seconds % 3600) / 60);
    }
}
