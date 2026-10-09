package ru.putevodika.route.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.putevodika.route.entity.RoutePlace;

import java.util.Collection;
import java.util.List;

public interface RoutePlaceRepository extends JpaRepository<RoutePlace, Long> {

    @EntityGraph(attributePaths = "place")
    List<RoutePlace> findAllByRoute_IdOrderByPositionAsc(Long routeId);

    interface RouteStopCount {
        Long getRouteId();
        Long getStopCount();
    }

    @Query("""
            select rp.route.id as routeId, count(rp.id) as stopCount
            from RoutePlace rp
            where rp.route.id in :ids
            group by rp.route.id
            """)
    List<RouteStopCount> countStopsForRoutes(@Param("ids") Collection<Long> ids);
}
