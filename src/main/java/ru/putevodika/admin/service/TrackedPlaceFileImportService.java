package ru.putevodika.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.putevodika.place.dto.OsmPlaceBatchImportResponse;
import ru.putevodika.place.service.PlaceFileImportService;

@Service
@RequiredArgsConstructor
public class TrackedPlaceFileImportService {
    private final ImportHistoryService history;
    private final PlaceFileImportService delegate;

    public OsmPlaceBatchImportResponse importFile(MultipartFile file) {
        Long jobId = history.start(file == null ? null : file.getOriginalFilename(),
                file == null ? 0 : file.getSize());
        try {
            OsmPlaceBatchImportResponse result = delegate.importFile(file);
            history.complete(jobId, result);
            return result;
        } catch (RuntimeException | Error exception) {
            try { history.fail(jobId, exception.getMessage()); }
            catch (RuntimeException trackingError) { exception.addSuppressed(trackingError); }
            throw exception;
        }
    }
}
