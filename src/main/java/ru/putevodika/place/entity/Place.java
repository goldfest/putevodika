package ru.putevodika.place.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "name",
            nullable = false,
            length = 255
    )
    private String name;

    @Column(name = "description")
    private String description;

    @Column(
            name = "address",
            length = 500
    )
    private String address;

    @Column(
            name = "location",
            nullable = false,
            columnDefinition = "geometry(Point,4326)"
    )
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "source_type",
            nullable = false,
            length = 32
    )
    private PlaceSourceType sourceType;

    @Column(
            name = "external_id",
            length = 128
    )
    private String externalId;

    @Column(name = "visit_duration_minutes")
    private Integer visitDurationMinutes;

    @Column(
            name = "opening_hours",
            length = 255
    )
    private String openingHours;

    @Column(
            name = "active",
            nullable = false
    )
    private boolean active;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "place_category",

            joinColumns = @JoinColumn(
                    name = "place_id"
            ),

            inverseJoinColumns = @JoinColumn(
                    name = "category_id"
            )
    )
    private Set<Category> categories = new HashSet<>();


    public Place(
            String name,
            String description,
            String address,
            Point location,
            PlaceSourceType sourceType,
            String externalId
    ) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.location = location;
        this.sourceType = sourceType;
        this.externalId = externalId;
        this.active = true;
    }


    public void addCategory(Category category) {
        categories.add(category);
    }


    public void deactivate() {
        active = false;
    }

    public void activate() {
        active = true;
    }


    @PrePersist
    void prePersist() {

        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;
    }


    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void update(
            String name,
            String description,
            String address,
            Point location
    ) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.location = location;
    }

    public void replaceCategories(Set<Category> categories) {
        this.categories.clear();
        this.categories.addAll(categories);
    }
}