package ru.putevodika.admin.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.putevodika.admin.service.AdminDashboardStatsService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardStatsService statsService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Главная панель");
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("stats", statsService.load());
        return "admin/dashboard";
    }
}
