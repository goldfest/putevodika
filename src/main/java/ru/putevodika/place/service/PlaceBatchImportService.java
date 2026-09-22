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
                boolean alreadyExists =
                        placeRepository
                                .existsBySourceTypeAndExternalId(
                                        PlaceSourceType.OSM,
                                        request.getId()
                                );

                placeImportService.importOne(request);

                if (alreadyExists) {
                    updated++;
                } else {
                    created++;
                }

            } catch (Exception exception) {

                failures.add(
                        OsmPlaceBatchImportResponse.Failure
                                .builder()
                                .id(request.getId())
                                .error(
                                        exception.getMessage()
                                )
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
}