package ru.putevodika.place.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "place_score")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceScore {

    @Id
    @Column(name = "place_id")
    private Long placeId;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @MapsId
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal nature;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal attractions;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal military;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal religion;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal architecture;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal history;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal art;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal souvenirs;

    @Column(
            name = "transport_tech",
            nullable = false,
            precision = 4,
            scale = 3
    )
    private BigDecimal transportTech;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal accommodation;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal food;

    @Column(
            name = "exclusive_food",
            nullable = false,
            precision = 4,
            scale = 3
    )
    private BigDecimal exclusiveFood;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal subcultures;

    public PlaceScore(Place place) {
        this.place = place;
    }

    public void update(
            BigDecimal nature,
            BigDecimal attractions,
            BigDecimal military,
            BigDecimal religion,
            BigDecimal architecture,
            BigDecimal history,
            BigDecimal art,
            BigDecimal souvenirs,
            BigDecimal transportTech,
            BigDecimal accommodation,
            BigDecimal food,
            BigDecimal exclusiveFood,
            BigDecimal subcultures
    ) {
        this.nature = nature;
        this.attractions = attractions;
        this.military = military;
        this.religion = religion;
        this.architecture = architecture;
        this.history = history;
        this.art = art;
        this.souvenirs = souvenirs;
        this.transportTech = transportTech;
        this.accommodation = accommodation;
        this.food = food;
        this.exclusiveFood = exclusiveFood;
        this.subcultures = subcultures;
    }
}