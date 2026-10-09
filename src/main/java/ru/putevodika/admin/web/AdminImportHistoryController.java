package ru.putevodika.admin.web;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.putevodika.admin.service.ImportHistoryService;

import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/imports")
@RequiredArgsConstructor
public class AdminImportHistoryController {
    private final ImportHistoryService history;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "") String status, Model model) {
        int requested = Math.max(0, page);
        Page<ImportHistoryService.ImportJobRow> results = history.findAll(requested, status);
        if (results.getTotalPages() > 0 && requested >= results.getTotalPages()) {
            results = history.findAll(results.getTotalPages() - 1, status);
        }
        int current = results.getNumber();
        model.addAttribute("pageTitle", "История импортов");
        model.addAttribute("activePage", "imports");
        model.addAttribute("jobs", results.getContent());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("page", current);
        model.addAttribute("totalPages", results.getTotalPages());
        model.addAttribute("totalElements", results.getTotalElements());
        model.addAttribute("pages", results.getTotalPages() == 0 ? new int[0] :
                IntStream.rangeClosed(Math.max(0, current - 2),
                        Math.min(results.getTotalPages() - 1, current + 2)).toArray());
        return "admin/imports/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Результат импорта");
        model.addAttribute("activePage", "imports");
        model.addAttribute("job", history.getById(id));
        return "admin/imports/details";
    }
}
