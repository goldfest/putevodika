package ru.putevodika.admin.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.putevodika.place.dto.OsmPlaceBatchImportResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admin_import_job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImportJob {
    public enum Status { RUNNING, COMPLETED, COMPLETED_WITH_ERRORS, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private Status status;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "created_count", nullable = false)
    private int createdCount;

    @Column(name = "updated_count", nullable = false)
    private int updatedCount;

    @Column(name = "failed_count", nullable = false)
    private int failedCount;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "admin_import_job_failure", joinColumns = @JoinColumn(name = "job_id"))
    @OrderColumn(name = "item_order")
    private List<ImportJobFailure> failures = new ArrayList<>();

    public ImportJob(Long actorUserId, String fileName, long fileSizeBytes) {
        this.actorUserId = actorUserId;
        this.fileName = fileName;
        this.fileSizeBytes = fileSizeBytes;
        this.status = Status.RUNNING;
        this.startedAt = Instant.now();
    }

    public void complete(OsmPlaceBatchImportResponse report) {
        this.totalCount = report.getTotal();
        this.createdCount = report.getCreated();
        this.updatedCount = report.getUpdated();
        this.failedCount = report.getFailed();
        this.status = report.getFailed() > 0 ? Status.COMPLETED_WITH_ERRORS : Status.COMPLETED;
        this.finishedAt = Instant.now();
        if (report.getFailures() != null) {
            report.getFailures().stream().limit(200)
                    .forEach(f -> failures.add(new ImportJobFailure(f.getId(), f.getError())));
        }
    }

    public void fail(String message) {
        this.status = Status.FAILED;
        this.finishedAt = Instant.now();
        this.errorMessage = message == null ? "Импорт прерван" : message.substring(0, Math.min(message.length(), 1000));
    }
}
