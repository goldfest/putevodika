package ru.putevodika.admin.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.putevodika.admin.entity.AdminAuditLog;
import ru.putevodika.admin.repository.AdminAuditLogRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAuditLogService {
    private final AdminAuditLogRepository repository;

    @Transactional
    public void record(String action, String subjectType, String subjectId, String details) {
        Long actor = currentActorId();
        String clientIp = currentClientIp();
        repository.save(new AdminAuditLog(actor, limited(action, 80),
                limited(subjectType, 40), limited(subjectId, 128),
                limited(details, 500), limited(clientIp, 64)));
    }

    @Transactional(readOnly = true)
    public Page<AdminAuditLog> findAll(int requestedPage, String action, String subjectType) {
        Specification<AdminAuditLog> spec = Specification.allOf(
                (root, query, cb) -> action == null || action.isBlank()
                        ? cb.conjunction() : cb.equal(root.get("action"), action),
                (root, query, cb) -> subjectType == null || subjectType.isBlank()
                        ? cb.conjunction() : cb.equal(root.get("subjectType"), subjectType)
        );
        return repository.findAll(spec, PageRequest.of(Math.max(0, requestedPage), 25,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
    }

    @Transactional(readOnly = true)
    public List<String> findActions() {
        return List.of("USER_ROLE_SET", "USER_DEACTIVATED", "USER_ACTIVATED",
                "PLACE_CREATED", "PLACE_UPDATED", "PLACE_DEACTIVATED", "PLACE_ACTIVATED",
                "IMPORT_COMPLETED", "IMPORT_FAILED");
    }

    public Long currentActorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) return null;
        try { return Long.valueOf(jwt.getSubject()); }
        catch (NumberFormatException exception) { return null; }
    }

    private String currentClientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest request = attrs.getRequest();
            return request.getRemoteAddr();
        }
        return null;
    }

    private static String limited(String value, int max) {
        return value == null ? null : value.substring(0, Math.min(value.length(), max));
    }
}
