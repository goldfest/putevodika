package ru.putevodika.admin.web;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.putevodika.admin.dto.AdminRouteViews;
import ru.putevodika.admin.service.AdminRouteService;
import ru.putevodika.common.dto.PageResponse;
import ru.putevodika.route.exception.RouteNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/routes")
@RequiredArgsConstructor
public class AdminRoutesPageController {
    private static final int PAGE_SIZE = 20;
    private final AdminRouteService adminRouteService;

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model
    ) {
        String search = q == null ? "" : q.strip();
        if (search.length() > 100) search = search.substring(0, 100);
        int requestedPage = Math.max(page, 0);
        boolean invalidDates = dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo);
        boolean invalidUserId = userId != null && userId < 1;

        PageResponse<AdminRouteViews.RouteRow> result;
        if (invalidDates || invalidUserId) {
            result = new PageResponse<>(List.of(), 0, PAGE_SIZE, 0L, 0, true, true);
            model.addAttribute("filterError", invalidDates
                    ? "Начальная дата не может быть позже конечной."
                    : "ID пользователя должен быть положительным числом.");
        } else {
            result = adminRouteService.findAll(requestedPage, PAGE_SIZE,
                    search, userId, dateFrom, dateTo);
            if (result.totalPages() > 0 && requestedPage >= result.totalPages()) {
                result = adminRouteService.findAll(result.totalPages() - 1,
                        PAGE_SIZE, search, userId, dateFrom, dateTo);
            }
        }

        int current = result.totalPages() == 0 ? 0 : result.page();
        int first = Math.max(0, current - 2);
        int last = Math.min(result.totalPages() - 1, current + 2);
        List<Integer> pages = result.totalPages() == 0 ? List.of()
                : IntStream.rangeClosed(first, last).boxed().toList();

        model.addAttribute("pageTitle", "Сохранённые маршруты");
        model.addAttribute("activePage", "routes");
        model.addAttribute("routes", result.content());
        model.addAttribute("page", current);
        model.addAttribute("totalElements", result.totalElements());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("pageNumbers", pages);
        model.addAttribute("q", search);
        model.addAttribute("userId", userId);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        return "admin/routes/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Маршрут № " + id);
        model.addAttribute("activePage", "routes");
        model.addAttribute("route", adminRouteService.getById(id));
        return "admin/routes/detail";
    }

    @ExceptionHandler(RouteNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(Model model) {
        model.addAttribute("pageTitle", "Маршрут не найден");
        model.addAttribute("activePage", "routes");
        return "admin/routes/not-found";
    }
}
