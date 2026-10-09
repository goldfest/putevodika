package ru.putevodika.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.putevodika.place.dto.CreatePlaceRequest;
import ru.putevodika.place.dto.PlaceResponse;
import ru.putevodika.place.dto.UpdatePlaceRequest;
import ru.putevodika.user.dto.ChangeUserRoleRequest;

/** Records successful service operations. The advice runs after the transaction proxy. */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class AdminAuditAspect {
    private final AdminAuditLogService logService;

    @AfterReturning(pointcut = "execution(* ru.putevodika.user.service.AdminUserService.changeRole(..)) && args(administratorId,userId,request)")
    public void userRole(Long administratorId, Long userId, ChangeUserRoleRequest request) {
        record("USER_ROLE_SET", "USER", String.valueOf(userId),
                "Роль установлена: " + request.getRole());
    }

    @AfterReturning(pointcut = "execution(* ru.putevodika.user.service.AdminUserService.deactivate(..)) && args(administratorId,userId)")
    public void userDeactivate(Long administratorId, Long userId) {
        record("USER_DEACTIVATED", "USER", String.valueOf(userId), "Запрошена блокировка");
    }

    @AfterReturning(pointcut = "execution(* ru.putevodika.user.service.AdminUserService.activate(..)) && args(userId)")
    public void userActivate(Long userId) {
        record("USER_ACTIVATED", "USER", String.valueOf(userId), "Запрошено восстановление");
    }

    @AfterReturning(pointcut = "execution(* ru.putevodika.place.service.PlaceService.create(..)) && args(request)", returning = "result")
    public void placeCreate(CreatePlaceRequest request, PlaceResponse result) {
        record("PLACE_CREATED", "PLACE", String.valueOf(result.getId()), "Объект создан");
    }

    @AfterReturning(pointcut = "execution(* ru.putevodika.place.service.PlaceService.update(..)) && args(id,request)")
    public void placeUpdate(Long id, UpdatePlaceRequest request) {
        record("PLACE_UPDATED", "PLACE", String.valueOf(id), "Поля объекта обновлены");
    }

    @AfterReturning(pointcut = "execution(* ru.putevodika.place.service.PlaceService.deactivate(..)) && args(id)")
    public void placeDeactivate(Long id) {
        record("PLACE_DEACTIVATED", "PLACE", String.valueOf(id), "Запрошено отключение");
    }

    @AfterReturning(pointcut = "execution(* ru.putevodika.place.service.PlaceService.activate(..)) && args(id)")
    public void placeActivate(Long id) {
        record("PLACE_ACTIVATED", "PLACE", String.valueOf(id), "Запрошено восстановление");
    }

    private void record(String action, String type, String id, String message) {
        try {
            logService.record(action, type, id, message);
        } catch (RuntimeException exception) {
            // A successful business operation must not appear to fail due to a secondary audit DB error.
            log.error("Не удалось сохранить аудит {} {} {}", action, type, id, exception);
        }
    }
}
