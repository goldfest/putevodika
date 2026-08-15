package ru.putevodika.feature.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.putevodika.user.entity.UserAccount;

@Entity
@Table(
        name = "user_feature_preference",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_feature_preference",
                columnNames = {
                        "user_id",
                        "feature_id"
                }
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFeaturePreference {

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
            name = "weight",
            nullable = false
    )
    private short weight;


    public UserFeaturePreference(
            UserAccount user,
            Feature feature,
            short weight
    ) {
        this.user = user;
        this.feature = feature;
        this.weight = weight;
    }
}