package ru.putevodika.feature.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.putevodika.place.entity.Place;

@Entity
@Table(
        name = "place_feature",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_place_feature",
                columnNames = {
                        "place_id",
                        "feature_id"
                }
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "place_id",
            nullable = false
    )
    private Place place;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "feature_id",
            nullable = false
    )
    private Feature feature;

    @Column(
            name = "value",
            nullable = false
    )
    private short value;


    public PlaceFeature(
            Place place,
            Feature feature,
            short value
    ) {
        this.place = place;
        this.feature = feature;
        this.value = value;
    }
}