package ru.putevodika.place.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "code",
            nullable = false,
            unique = true,
            length = 64
    )
    private String code;

    @Column(
            name = "name",
            nullable = false,
            length = 128
    )
    private String name;

    @Column(
            name = "active",
            nullable = false
    )
    private boolean active;

    @Column(
            name = "preference_selectable",
            nullable = false
    )
    private boolean preferenceSelectable;
}