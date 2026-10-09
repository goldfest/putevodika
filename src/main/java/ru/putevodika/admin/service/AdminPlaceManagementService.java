package ru.putevodika.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.admin.dto.AdminPlaceForm;
import ru.putevodika.place.dto.PlaceResponse;
import ru.putevodika.place.entity.Place;
import ru.putevodika.place.exception.PlaceNotFoundException;
import ru.putevodika.place.repository.PlaceRepository;
import ru.putevodika.place.service.PlaceService;

@Service
@RequiredArgsConstructor
public class AdminPlaceManagementService {

    private final PlaceService placeService;
    private final PlaceRepository placeRepository;

    @Transactional
    public Long create(AdminPlaceForm form) {
        PlaceResponse created = placeService.create(form.toCreateRequest());
        updateContacts(created.getId(), form);
        return created.getId();
    }

    @Transactional
    public void update(Long id, AdminPlaceForm form) {
        placeService.update(id, form.toUpdateRequest());
        updateContacts(id, form);
    }

    private void updateContacts(Long id, AdminPlaceForm form) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new PlaceNotFoundException(id));
        place.updateContacts(
                blankToNull(form.getPhone()),
                blankToNull(form.getWebsite()),
                blankToNull(form.getEmail())
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
