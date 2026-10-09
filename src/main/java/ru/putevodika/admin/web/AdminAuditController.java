package ru.putevodika.admin.web;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.putevodika.admin.entity.AdminAuditLog;
import ru.putevodika.admin.service.AdminAuditLogService;

import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
public class AdminAuditController {
    private final AdminAuditLogService service;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "") String action,
                       @RequestParam(defaultValue = "") String type, Model model) {
        int requested = Math.max(0, page);
        Page<AdminAuditLog> result = service.findAll(requested, action, type);
        if (result.getTotalPages() > 0 && requested >= result.getTotalPages()) {
            result = service.findAll(result.getTotalPages() - 1, action, type);
        }
        int current = result.getNumber();
        model.addAttribute("pageTitle", "Журнал действий");
        model.addAttribute("activePage", "audit");
        model.addAttribute("entries", result.getContent());
        model.addAttribute("actions", service.findActions());
        model.addAttribute("selectedAction", action);
        model.addAttribute("selectedType", type);
        model.addAttribute("page", current);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());
        model.addAttribute("pages", result.getTotalPages() == 0 ? new int[0] :
                IntStream.rangeClosed(Math.max(0, current - 2),
                        Math.min(result.getTotalPages() - 1, current + 2)).toArray());
        return "admin/audit/list";
    }
}
