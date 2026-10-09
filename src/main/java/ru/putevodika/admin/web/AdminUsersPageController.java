package ru.putevodika.admin.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.putevodika.common.dto.PageResponse;
import ru.putevodika.user.dto.ChangeUserRoleRequest;
import ru.putevodika.user.dto.UserListItemResponse;
import ru.putevodika.user.dto.UserResponse;
import ru.putevodika.user.entity.UserRole;
import ru.putevodika.user.exception.SelfAdministrationException;
import ru.putevodika.user.exception.UserNotFoundException;
import ru.putevodika.user.service.AdminUserService;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUsersPageController {

    private static final int PAGE_SIZE = 20;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm 'UTC'")
                    .withZone(ZoneOffset.UTC);

    private final AdminUserService adminUserService;

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "ALL") String role,
            @RequestParam(defaultValue = "ALL") String active,
            Model model
    ) {
        String cleanSearch = search.trim();
        if (cleanSearch.length() > 100) {
            cleanSearch = cleanSearch.substring(0, 100);
        }

        UserRole roleFilter = switch (role) {
            case "USER" -> UserRole.USER;
            case "ADMIN" -> UserRole.ADMIN;
            default -> null;
        };
        Boolean activeFilter = switch (active) {
            case "true" -> true;
            case "false" -> false;
            default -> null;
        };

        int requestedPage = Math.max(0, page);
        PageResponse<UserListItemResponse> result = adminUserService.findAll(
                requestedPage, PAGE_SIZE, activeFilter, roleFilter, cleanSearch
        );

        if (result.totalPages() > 0 && requestedPage >= result.totalPages()) {
            result = adminUserService.findAll(
                    result.totalPages() - 1, PAGE_SIZE,
                    activeFilter, roleFilter, cleanSearch
            );
        }

        int currentPage = result.page();
        int from = Math.max(0, currentPage - 2);
        int to = Math.min(result.totalPages() - 1, currentPage + 2);
        List<Integer> pageNumbers = result.totalPages() == 0
                ? List.of()
                : IntStream.rangeClosed(from, to).boxed().toList();

        model.addAttribute("pageTitle", "Пользователи");
        model.addAttribute("activePage", "users");
        model.addAttribute("users", result.content().stream().map(user ->
                new UserRow(
                        user.getId(), user.getEmail(), user.getDisplayName(),
                        user.getRole(), user.isActive(),
                        format(user.getCreatedAt())
                )
        ).toList());
        model.addAttribute("search", cleanSearch);
        model.addAttribute("selectedRole", roleFilter == null ? "ALL" : roleFilter.name());
        model.addAttribute("selectedActive", activeFilter == null ? "ALL" : activeFilter.toString());
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("totalElements", result.totalElements());
        model.addAttribute("pageNumbers", pageNumbers);
        return "admin/users/list";
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            UserResponse user = adminUserService.getById(id);
            model.addAttribute("pageTitle", "Карточка пользователя");
            model.addAttribute("activePage", "users");
            model.addAttribute("user", user);
            model.addAttribute("isSelf", Long.valueOf(jwt.getSubject()).equals(id));
            model.addAttribute("createdAt", format(user.getCreatedAt()));
            model.addAttribute("updatedAt", format(user.getUpdatedAt()));
            model.addAttribute("onboardingAt", format(user.getOnboardingCompletedAt()));
            return "admin/users/detail";
        } catch (UserNotFoundException exception) {
            redirectAttributes.addFlashAttribute("error", "Пользователь не найден.");
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/{id}/role")
    public String changeRole(
            @PathVariable Long id,
            @RequestParam UserRole role,
            @AuthenticationPrincipal Jwt jwt,
            RedirectAttributes redirectAttributes
    ) {
        ChangeUserRoleRequest request = new ChangeUserRoleRequest();
        request.setRole(role);
        try {
            adminUserService.changeRole(Long.valueOf(jwt.getSubject()), id, request);
            redirectAttributes.addFlashAttribute("success", "Роль пользователя обновлена. Его прежние сессии завершены.");
        } catch (SelfAdministrationException exception) {
            redirectAttributes.addFlashAttribute("error", "Нельзя изменять собственную роль.");
        } catch (UserNotFoundException exception) {
            redirectAttributes.addFlashAttribute("error", "Пользователь не найден.");
            return "redirect:/admin/users";
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminUserService.deactivate(Long.valueOf(jwt.getSubject()), id);
            redirectAttributes.addFlashAttribute("success", "Пользователь заблокирован. Активные сессии отозваны.");
        } catch (SelfAdministrationException exception) {
            redirectAttributes.addFlashAttribute("error", "Нельзя блокировать собственную учётную запись.");
        } catch (UserNotFoundException exception) {
            redirectAttributes.addFlashAttribute("error", "Пользователь не найден.");
            return "redirect:/admin/users";
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/activate")
    public String activate(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            RedirectAttributes redirectAttributes
    ) {
        if (Long.valueOf(jwt.getSubject()).equals(id)) {
            redirectAttributes.addFlashAttribute("error", "Нельзя менять активность собственной учётной записи.");
            return "redirect:/admin/users/" + id;
        }
        try {
            adminUserService.activate(id);
            redirectAttributes.addFlashAttribute("success", "Учётная запись восстановлена.");
        } catch (UserNotFoundException exception) {
            redirectAttributes.addFlashAttribute("error", "Пользователь не найден.");
            return "redirect:/admin/users";
        }
        return "redirect:/admin/users/" + id;
    }

    private static String format(Instant date) {
        return date == null ? "—" : DATE_FORMAT.format(date);
    }

    public record UserRow(
            Long id,
            String email,
            String displayName,
            String role,
            boolean active,
            String createdAt
    ) {}
}
