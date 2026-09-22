package ru.putevodika.place.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class OsmPlaceImportRequest {

    @NotBlank
    @Size(max = 128)
    private String id;

    @NotNull
    @Valid
    private OsmData osm;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotEmpty
    private Set<@NotBlank String> categories;

    @Valid
    private DescriptionData description;

    @NotNull
    @Valid
    private LocationData location;

    @JsonProperty("opening_hours")
    @Size(max = 255)
    private String openingHours;

    @Valid
    private ContactsData contacts;

    @NotNull
    @Valid
    private PlaceScoresRequest scores;

    @JsonProperty("available_for_route")
    private boolean availableForRoute = true;

    private List<String> warnings =
            new ArrayList<>();


    @Getter
    @Setter
    public static class OsmData {

        @NotBlank
        @Pattern(
                regexp = "node|way|relation",
                message = "OSM type must be node, way or relation"
        )
        private String type;

        @NotNull
        @Positive
        private Long id;

        @NotNull
        private Map<String, String> tags =
                new HashMap<>();
    }


    @Getter
    @Setter
    public static class DescriptionData {

        private String shortText;

        private String full;

        @JsonProperty("short")
        public void setShortText(String shortText) {
            this.shortText = shortText;
        }

        @JsonProperty("short")
        public String getShortText() {
            return shortText;
        }
    }


    @Getter
    @Setter
    public static class LocationData {

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        private Double lat;

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        private Double lon;

        @JsonProperty("geometry_type")
        @Size(max = 32)
        private String geometryType;

        @JsonProperty("point_source")
        @Size(max = 64)
        private String pointSource;
    }


    @Getter
    @Setter
    public static class ContactsData {

        @Size(max = 64)
        private String phone;

        @Size(max = 2048)
        private String website;

        @Email
        @Size(max = 320)
        private String email;
    }
}