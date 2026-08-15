package ru.putevodika.route.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import ru.putevodika.user.entity.UserAccount;

import java.time.Instant;

@Entity
@Table(name = "saved_route")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
            name = "start_point",
            nullable = false,
            columnDefinition = "geometry(Point,4326)"
    )
    private Point startPoint;

    @Column(
            name = "finish_point",
            nullable = false,
            columnDefinition = "geometry(Point,4326)"
    )
    private Point finishPoint;

    @Column(name = "walking_distance_meters")
    private Integer walkingDistanceMeters;

    @Column(name = "walking_duration_seconds")
    private Integer walkingDurationSeconds;

    @Column(name = "total_duration_minutes")
    private Integer totalDurationMinutes;

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


    public SavedRoute(
            UserAccount user,
            Point startPoint,
            Point finishPoint
    ) {
        this.user = user;
        this.startPoint = startPoint;
        this.finishPoint = finishPoint;
    }


    public void updateMetrics(
            Integer walkingDistanceMeters,
            Integer walkingDurationSeconds,
            Integer totalDurationMinutes
    ) {
        this.walkingDistanceMeters =
                walkingDistanceMeters;

        this.walkingDurationSeconds =
                walkingDurationSeconds;

        this.totalDurationMinutes =
                totalDurationMinutes;
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