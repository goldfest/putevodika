package ru.putevodika.admin.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.putevodika.admin.service.TrackedPlaceFileImportService;
import ru.putevodika.place.dto.OsmPlaceBatchImportResponse;
import ru.putevodika.place.exception.InvalidOsmImportFileException;
import java.util.List;
import java.util.Locale;

@Slf4j
@Controller
@RequestMapping("/admin/import/osm")
@RequiredArgsConstructor
public class AdminOsmImportController {
    private static final int MAX_VISIBLE_FAILURES = 200;
    private final TrackedPlaceFileImportService trackedFileImportService;

    @GetMapping
    public String form(Model model) {
        preparePage(model);
        return "admin/import/osm";
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@RequestParam(value = "file", required = false) MultipartFile file,
                         Model model, HttpServletResponse response) {
        preparePage(model);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        if (file == null || file.isEmpty()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            model.addAttribute("error", "Выберите непустой JSON-файл для импорта.");
            return "admin/import/osm";
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase(Locale.ROOT).endsWith(".json")) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            model.addAttribute("error", "Поддерживаются только файлы .json; ZIP-архивы загрузить нельзя.");
            return "admin/import/osm";
        }
        model.addAttribute("fileName", safeDisplayName(originalName));
        try {
            OsmPlaceBatchImportResponse report = trackedFileImportService.importFile(file);
            List<OsmPlaceBatchImportResponse.Failure> failures = report.getFailures();
            if (failures == null) failures = List.of();
            model.addAttribute("report", report);
            model.addAttribute("visibleFailures", failures.stream().limit(MAX_VISIBLE_FAILURES).toList());
            model.addAttribute("moreFailures", Math.max(0, failures.size() - MAX_VISIBLE_FAILURES));
        } catch (InvalidOsmImportFileException exception) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            model.addAttribute("error", shortMessage(exception.getMessage()));
        } catch (RuntimeException exception) {
            log.error("OSM import via admin page failed (file: {})", safeDisplayName(originalName), exception);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            model.addAttribute("error", "Импорт не удалось завершить из-за ошибки сервера. "
                    + "Часть ранее обработанных объектов могла сохраниться. "
                    + "Проверьте журнал backend перед повторной загрузкой.");
        }
        return "admin/import/osm";
    }

    private void preparePage(Model model) {
        model.addAttribute("pageTitle", "Импорт OSM");
        model.addAttribute("activePage", "import");
    }

    private String safeDisplayName(String name) {
        String normalized = name.replace('\\', '/');
        String lastPart = normalized.substring(normalized.lastIndexOf('/') + 1);
        return lastPart.length() > 120 ? lastPart.substring(0, 120) + "…" : lastPart;
    }

    private String shortMessage(String message) {
        if (message == null || message.isBlank()) return "Не удалось прочитать JSON-файл импорта.";
        return message.length() > 400 ? message.substring(0, 400) + "…" : message;
    }
}
