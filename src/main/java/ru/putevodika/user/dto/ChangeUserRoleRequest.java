package ru.putevodika.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.putevodika.user.entity.UserRole;

@Getter
@Setter
public class ChangeUserRoleRequest {

    @NotNull
    private UserRole role;
}