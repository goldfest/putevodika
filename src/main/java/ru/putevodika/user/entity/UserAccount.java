package ru.putevodika.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.putevodika.place.entity.Category;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "email",
            nullable = false,
            length = 320
    )
    private String email;

    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;

    @Column(
            name = "display_name",
            nullable = false,
            length = 100
    )
    private String displayName;

    @Column(
            name = "avatar_url",
            length = 2048
    )
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            length = 32
    )
    private UserRole role;

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
            name = "user_preferred_category",
            joinColumns = @JoinColumn(
                    name = "user_id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "category_id"
            )
    )
    private Set<Category> preferredCategories =
            new HashSet<>();

    public UserAccount(
            String email,
            String passwordHash,
            String displayName
    ) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = UserRole.USER;
        this.active = true;
    }

    public void replacePreferredCategories(
            Set<Category> categories
    ) {
        preferredCategories.clear();
        preferredCategories.addAll(categories);
    }

    public void changeDisplayName(
            String displayName
    ) {
        this.displayName = displayName;
    }

    public void changeAvatarUrl(
            String avatarUrl
    ) {
        this.avatarUrl = avatarUrl;
    }

    public void changePasswordHash(
            String passwordHash
    ) {
        this.passwordHash = passwordHash;
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

    public void changeRole(UserRole role) {
        this.role = role;
    }
}
