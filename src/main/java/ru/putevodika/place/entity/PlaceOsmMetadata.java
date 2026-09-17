package ru.putevodika.place.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "place_osm_metadata")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceOsmMetadata {

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

    @Enumerated(EnumType.STRING)
    @Column(
            name = "osm_type",
            nullable = false,
            length = 16
    )
    private OsmElementType osmType;

    @Column(
            name = "osm_id",
            nullable = false
    )
    private Long osmId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "tags",
            nullable = false,
            columnDefinition = "jsonb"
    )
    private Map<String, String> tags =
            new HashMap<>();

    public PlaceOsmMetadata(
            Place place,
            OsmElementType osmType,
            Long osmId,
            Map<String, String> tags
    ) {
        this.place = place;
        this.osmType = osmType;
        this.osmId = osmId;
        this.tags =
                tags == null
                        ? new HashMap<>()
                        : new HashMap<>(tags);
    }

    public void updateTags(
            Map<String, String> tags
    ) {
        this.tags =
                tags == null
                        ? new HashMap<>()
                        : new HashMap<>(tags);
    }

    @Column(
            name = "geometry_type",
            length = 32
    )
    private String geometryType;

    @Column(
            name = "point_source",
            length = 64
    )
    private String pointSource;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "warnings",
            nullable = false,
            columnDefinition = "jsonb"
    )
    private List<String> warnings =
            new ArrayList<>();

    public void updateImportMetadata(
            String geometryType,
            String pointSource,
            List<String> warnings
    ) {
        this.geometryType = geometryType;
        this.pointSource = pointSource;

        this.warnings =
                warnings == null
                        ? new ArrayList<>()
                        : new ArrayList<>(warnings);
    }
}