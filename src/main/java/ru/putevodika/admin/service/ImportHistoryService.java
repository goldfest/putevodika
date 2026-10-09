package ru.putevodika.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.putevodika.admin.entity.ImportJob;
import ru.putevodika.admin.entity.ImportJobFailure;
import ru.putevodika.admin.repository.ImportJobRepository;
import ru.putevodika.place.dto.OsmPlaceBatchImportResponse;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportHistoryService {
    private final ImportJobRepository jobs;
    private final AdminAuditLogService audit;

    @Transactional
    public Long start(String filename, long size) {
        String safeName = safeFilename(filename);
        ImportJob job = jobs.save(new ImportJob(audit.currentActorId(), safeName, size));
        return job.getId();
    }

    @Transactional
    public void complete(Long id, OsmPlaceBatchImportResponse result) {
        ImportJob job = required(id);
        job.complete(result);
        audit.record("IMPORT_COMPLETED", "IMPORT", String.valueOf(id),
                "Создано: " + result.getCreated() + ", обновлено: " + result.getUpdated()
                        + ", ошибок: " + result.getFailed());
    }

    @Transactional
    public void fail(Long id, String error) {
        ImportJob job = required(id);
        job.fail("Импорт прерван. Некоторые записи могли сохраниться. " +
                (error == null ? "" : error));
        audit.record("IMPORT_FAILED", "IMPORT", String.valueOf(id), "Импорт прерван");
    }

    @Transactional(readOnly = true)
    public Page<ImportJobRow> findAll(int page, String status) {
        ImportJob.Status selected = null;
        if (status != null && !status.isBlank()) {
            try { selected = ImportJob.Status.valueOf(status); }
            catch (IllegalArgumentException ignored) { /* Ignore invalid filter */ }
        }
        ImportJob.Status finalSelected = selected;
        Specification<ImportJob> specification = (root, query, cb) ->
                finalSelected == null ? cb.conjunction() : cb.equal(root.get("status"), finalSelected);
        return jobs.findAll(specification, PageRequest.of(Math.max(0, page), 20,
                Sort.by(Sort.Order.desc("startedAt"), Sort.Order.desc("id"))))
                .map(job -> new ImportJobRow(job.getId(), job.getFileName(), job.getFileSizeBytes(),
                        job.getStatus(), job.getActorUserId(), job.getStartedAt(), job.getFinishedAt(),
                        job.getTotalCount(), job.getCreatedCount(), job.getUpdatedCount(), job.getFailedCount()));
    }

    @Transactional(readOnly = true)
    public ImportJobDetails getById(Long id) {
        ImportJob job = required(id);
        List<ImportJobFailure> examples = List.copyOf(job.getFailures());
        return new ImportJobDetails(job.getId(), job.getFileName(), job.getFileSizeBytes(),
                job.getStatus(), job.getActorUserId(), job.getStartedAt(), job.getFinishedAt(),
                job.getTotalCount(), job.getCreatedCount(), job.getUpdatedCount(), job.getFailedCount(),
                job.getErrorMessage(), examples);
    }

    private ImportJob required(Long id) {
        return jobs.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Импорт не найден"));
    }

    private static String safeFilename(String original) {
        String cleaned = original == null ? "неизвестный файл" : original.replace('\\', '/');
        String result = cleaned.substring(cleaned.lastIndexOf('/') + 1);
        return result.substring(0, Math.min(result.length(), 255));
    }

    private static String formatUtc(Instant instant) {
        return instant == null ? "—" : DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm 'UTC'")
                .withZone(ZoneOffset.UTC).format(instant);
    }

    public record ImportJobRow(Long id, String fileName, long fileSizeBytes,
                               ImportJob.Status status, Long actorUserId,
                               Instant startedAt, Instant finishedAt,
                               int totalCount, int createdCount, int updatedCount, int failedCount) {
        public String getStartedAtDisplay() { return formatUtc(startedAt); }
    }

    public record ImportJobDetails(Long id, String fileName, long fileSizeBytes,
                                   ImportJob.Status status, Long actorUserId,
                                   Instant startedAt, Instant finishedAt,
                                   int totalCount, int createdCount, int updatedCount, int failedCount,
                                   String errorMessage, List<ImportJobFailure> failures) {
        public String getStartedAtDisplay() { return formatUtc(startedAt); }
        public String getFinishedAtDisplay() { return formatUtc(finishedAt); }
    }
}
