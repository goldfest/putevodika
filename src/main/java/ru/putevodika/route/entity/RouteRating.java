package ru.putevodika.route.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.putevodika.user.entity.UserAccount;

import java.time.Instant;

@Entity
@Table(
        name = "route_rating",
        uniqueConstraints =
        @UniqueConstraint(
                name = "uq_route_rating_user",
                columnNames = {
                        "route_id",
                        "user_id"
                }
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RouteRating {

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
            name = "user_id",
            nullable = false
    )
    private UserAccount user;

    @Column(
            name = "value",
            nullable = false
    )
    private short value;

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


    public RouteRating(
            SavedRoute route,
            UserAccount user,
            short value
    ) {
        this.route = route;
        this.user = user;
        this.value = value;
    }


    public void changeValue(short value) {
        this.value = value;
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
}