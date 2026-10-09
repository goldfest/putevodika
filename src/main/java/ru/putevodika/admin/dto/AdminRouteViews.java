package ru.putevodika.admin.dto;

import java.util.List;

/** Read-only views for the administrator, not the public route API. */
public final class AdminRouteViews {
    private AdminRouteViews() { }

    public record RouteRow(
            Long id,
            Long userId,
            String userName,
            String userEmail,
            long stopCount,
            String distance,
            String duration,
            String createdAt
    ) { }

    public record GeoPoint(Double latitude, Double longitude) { }

    public record RouteStop(
            int position,
            Long placeId,
            String name,
            String address,
            Double latitude,
            Double longitude,
            boolean active,
            boolean availableForRoute
    ) { }

    public record RouteDetail(
            Long id,
            Long userId,
            String userName,
            String userEmail,
            GeoPoint start,
            GeoPoint finish,
            String distance,
            String walkingDuration,
            String totalDuration,
            Integer ownerRating,
            String createdAt,
            String updatedAt,
            List<RouteStop> stops
    ) { }
}
