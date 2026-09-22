package ru.putevodika.place.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.putevodika.place.dto.OsmPlaceImportRequest;
import ru.putevodika.place.dto.PlaceResponse;
import ru.putevodika.place.service.PlaceImportService;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import ru.putevodika.place.dto.OsmPlaceBatchImportResponse;
import ru.putevodika.place.service.PlaceBatchImportService;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import ru.putevodika.place.service.PlaceFileImportService;

@RestController
@RequestMapping("/api/v1/admin/places/import")
@RequiredArgsConstructor
@Tag(
        name = "Импорт туристических объектов",
        description = "Импорт подготовленных туристических объектов"
)
@SecurityRequirement(name = "bearerAuth")
public class PlaceImportController {

    private final PlaceImportService placeImportService;

    private final PlaceBatchImportService
            placeBatchImportService;

    private final PlaceFileImportService
            placeFileImportService;

    @PostMapping("/osm")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceResponse importOsm(
            @Valid @RequestBody
            OsmPlaceImportRequest request
    ) {
        return placeImportService.importOne(request);
    }

    @PostMapping("/osm/batch")
    public OsmPlaceBatchImportResponse importOsmBatch(
            @NotEmpty
            @RequestBody
            List<@Valid OsmPlaceImportRequest> requests
    ) {
        return placeBatchImportService.importAll(
                requests
        );
    }

    @PostMapping(
            value = "/osm/file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public OsmPlaceBatchImportResponse importOsmFile(
            @RequestParam("file")
            MultipartFile file
    ) {
        return placeFileImportService
                .importFile(file);
    }
}