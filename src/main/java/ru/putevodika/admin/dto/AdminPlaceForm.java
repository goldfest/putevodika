package ru.putevodika.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import ru.putevodika.place.dto.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class AdminPlaceForm {

    @NotBlank(message = "Укажите название")
    @Size(max = 255)
    private String name;

    private String description;

    @Size(max = 500)
    private String address;

    @NotNull(message = "Укажите широту")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    @NotNull(message = "Укажите долготу")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;

    @NotEmpty(message = "Выберите хотя бы одну категорию")
    private Set<String> categories = new HashSet<>();

    @Min(1)
    @Max(1440)
    private Integer visitDurationMinutes;

    @Size(max = 255)
    private String openingHours;

    // null означает, что характеристика не задана, 0 — явная оценка 0.
    @Size(max = 50)
    private Map<@NotBlank String, @Min(0) @Max(5) Integer> featureValues =
            new HashMap<>();

    @NotNull
    @Valid
    private PlaceScoresRequest scores = zeroScores();

    private boolean availableForRoute = true;

    @Size(max = 64)
    private String phone;

    @Size(max = 2048)
    private String website;

    @Email
    @Size(max = 320)
    private String email;

    public static AdminPlaceForm from(PlaceResponse place) {
        AdminPlaceForm form = new AdminPlaceForm();
        form.setName(place.getName());
        form.setDescription(place.getDescription());
        form.setAddress(place.getAddress());
        form.setLatitude(place.getLatitude());
        form.setLongitude(place.getLongitude());
        form.setCategories(new HashSet<>(place.getCategories()));
        form.setVisitDurationMinutes(place.getVisitDurationMinutes());
        form.setOpeningHours(place.getOpeningHours());
        form.setFeatureValues(new HashMap<>(place.getFeatures()));
        form.setAvailableForRoute(place.isAvailableForRoute());
        form.setPhone(place.getPhone());
        form.setWebsite(place.getWebsite());
        form.setEmail(place.getEmail());
        PlaceScoresResponse s = place.getScores();
        PlaceScoresRequest req = new PlaceScoresRequest();
        req.setNature(s.getNature());
        req.setAttractions(s.getAttractions());
        req.setMilitary(s.getMilitary());
        req.setReligion(s.getReligion());
        req.setArchitecture(s.getArchitecture());
        req.setHistory(s.getHistory());
        req.setArt(s.getArt());
        req.setSouvenirs(s.getSouvenirs());
        req.setTransportTech(s.getTransportTech());
        req.setAccommodation(s.getAccommodation());
        req.setFood(s.getFood());
        req.setExclusiveFood(s.getExclusiveFood());
        req.setSubcultures(s.getSubcultures());
        form.setScores(req);
        return form;
    }

    public CreatePlaceRequest toCreateRequest() {
        CreatePlaceRequest request = new CreatePlaceRequest();
        request.setName(name);
        request.setDescription(description);
        request.setAddress(address);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setCategories(Set.copyOf(categories));
        request.setVisitDurationMinutes(visitDurationMinutes);
        request.setOpeningHours(openingHours);
        request.setFeatures(selectedFeatures());
        request.setScores(scores);
        request.setAvailableForRoute(availableForRoute);
        return request;
    }

    public UpdatePlaceRequest toUpdateRequest() {
        UpdatePlaceRequest request = new UpdatePlaceRequest();
        request.setName(name);
        request.setDescription(description);
        request.setAddress(address);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setCategories(Set.copyOf(categories));
        request.setVisitDurationMinutes(visitDurationMinutes);
        request.setOpeningHours(openingHours);
        request.setFeatures(selectedFeatures());
        request.setScores(scores);
        request.setAvailableForRoute(availableForRoute);
        return request;
    }

    private Map<String, Integer> selectedFeatures() {
        return featureValues.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static PlaceScoresRequest zeroScores() {
        PlaceScoresRequest request = new PlaceScoresRequest();
        request.setNature(BigDecimal.ZERO);
        request.setAttractions(BigDecimal.ZERO);
        request.setMilitary(BigDecimal.ZERO);
        request.setReligion(BigDecimal.ZERO);
        request.setArchitecture(BigDecimal.ZERO);
        request.setHistory(BigDecimal.ZERO);
        request.setArt(BigDecimal.ZERO);
        request.setSouvenirs(BigDecimal.ZERO);
        request.setTransportTech(BigDecimal.ZERO);
        request.setAccommodation(BigDecimal.ZERO);
        request.setFood(BigDecimal.ZERO);
        request.setExclusiveFood(BigDecimal.ZERO);
        request.setSubcultures(BigDecimal.ZERO);
        return request;
    }
}
