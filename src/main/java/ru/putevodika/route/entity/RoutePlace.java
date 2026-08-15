package ru.putevodika.route.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.putevodika.place.entity.Place;

@Entity
@Table(
        name = "route_place",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_route_place_position",
                        columnNames = {
                                "route_id",
                                "position"
                        }
                ),
                @UniqueConstraint(
                        name = "uq_route_place_place",
                        columnNames = {
                                "route_id",
                                "place_id"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutePlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "route_id",
            nullable = false
    )
    private SavedRoute route;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "place_id",
            nullable = false
    )
    private Place place;

    @Column(
            name = "position",
            nullable = false
    )
    private int position;


    public RoutePlace(
            SavedRoute route,
            Place place,
            int position
    ) {
        this.route = route;
        this.place = place;
        this.position = position;
    }
}