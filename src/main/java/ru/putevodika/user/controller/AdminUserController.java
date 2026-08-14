package ru.putevodika.user.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.putevodika.common.dto.PageResponse;
import ru.putevodika.user.dto.ChangeUserRoleRequest;
import ru.putevodika.user.dto.UserListItemResponse;
import ru.putevodika.user.dto.UserResponse;
import ru.putevodika.user.entity.UserRole;
import ru.putevodika.user.service.AdminUserService;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Администрирование пользователей",
        description = "Управление учетными записями пользователей"
)
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;


    @GetMapping
    public PageResponse<UserListItemResponse> findAll(
            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size,

            @RequestParam(required = false)
            Boolean active,

            @RequestParam(required = false)
            UserRole role,

            @RequestParam(required = false)
            String search
    ) {
        return adminUserService.findAll(
                page,
                size,
                active,
                role,
                search
        );
    }


    @GetMapping("/{id}")
    public UserResponse getById(
            @PathVariable Long id
    ) {
        return adminUserService.getById(id);
    }


    @PatchMapping("/{id}/role")
    public UserResponse changeRole(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody ChangeUserRoleRequest request
    ) {
        return adminUserService.changeRole(
                currentUserId(jwt),
                id,
                request
        );
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        adminUserService.deactivate(
                currentUserId(jwt),
                id
        );
    }


    @PatchMapping("/{id}/activate")
    public UserResponse activate(
            @PathVariable Long id
    ) {
        return adminUserService.activate(id);
    }


    private Long currentUserId(Jwt jwt) {
        return Long.valueOf(
                jwt.getSubject()
        );
    }
}