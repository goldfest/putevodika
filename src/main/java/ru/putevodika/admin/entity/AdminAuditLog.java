package ru.putevodika.admin.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "admin_audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "action", nullable = false, length = 80)
    private String action;

    @Column(name = "subject_type", nullable = false, length = 40)
    private String subjectType;

    @Column(name = "subject_id", length = 128)
    private String subjectId;

    @Column(name = "details", length = 500)
    private String details;

    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public String getCreatedAtDisplay() {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm 'UTC'")
                .withZone(ZoneOffset.UTC).format(createdAt);
    }

    public AdminAuditLog(Long actorUserId, String action, String subjectType,
                         String subjectId, String details, String clientIp) {
        this.actorUserId = actorUserId;
        this.action = action;
        this.subjectType = subjectType;
        this.subjectId = subjectId;
        this.details = details;
        this.clientIp = clientIp;
        this.createdAt = Instant.now();
    }
}
