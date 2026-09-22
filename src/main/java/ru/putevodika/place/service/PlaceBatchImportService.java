package ru.putevodika.place.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.putevodika.place.dto.OsmPlaceBatchImportResponse;
import ru.putevodika.place.dto.OsmPlaceImportRequest;
import ru.putevodika.place.entity.PlaceSourceType;
import ru.putevodika.place.repository.PlaceRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceBatchImportService {

    private final PlaceImportService placeImportService;

    private final PlaceRepository placeRepository;


    public OsmPlaceBatchImportResponse importAll(
            List<OsmPlaceImportRequest> requests
    ) {
        int created = 0;
        int updated = 0;

        List<OsmPlaceBatchImportResponse.Failure> failures =
                new ArrayList<>();

        for (OsmPlaceImportRequest request : requests) {

            try {
                ImportStatus status =
                        importWithStatus(request);

                if (status == ImportStatus.CREATED) {
                    created++;
                } else {
                    updated++;
                }

            } catch (Exception exception) {

                failures.add(
                        OsmPlaceBatchImportResponse.Failure
                                .builder()
                                .id(
                                        request == null
                                                ? null
                                                : request.getId()
                                )
                                .error(exception.getMessage())
                                .build()
                );
            }
        }

        return OsmPlaceBatchImportResponse
                .builder()
                .total(requests.size())
                .created(created)
                .updated(updated)
                .failed(failures.size())
                .failures(failures)
                .build();
    }


    public ImportStatus importWithStatus(
            OsmPlaceImportRequest request
    ) {
        boolean alreadyExists =
                placeRepository
                        .existsBySourceTypeAndExternalId(
                                PlaceSourceType.OSM,
                                request.getId()
                        );

        placeImportService.importOne(request);

        return alreadyExists
                ? ImportStatus.UPDATED
                : ImportStatus.CREATED;
    }


    public enum ImportStatus {
        CREATED,
        UPDATED
    }
}