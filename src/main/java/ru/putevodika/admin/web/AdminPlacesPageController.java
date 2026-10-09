package ru.putevodika.admin.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.putevodika.admin.dto.AdminPlaceForm;
import ru.putevodika.admin.service.AdminPlaceManagementService;
import ru.putevodika.common.dto.PageResponse;
import ru.putevodika.feature.exception.UnknownFeatureException;
import ru.putevodika.feature.service.FeatureService;
import ru.putevodika.place.dto.PlaceListItemResponse;
import ru.putevodika.place.dto.PlaceResponse;
import ru.putevodika.place.entity.PlaceSourceType;
import ru.putevodika.place.exception.PlaceNotFoundException;
import ru.putevodika.place.exception.UnknownPlaceCategoryException;
import ru.putevodika.place.service.CategoryService;
import ru.putevodika.place.service.PlaceService;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/places")
@RequiredArgsConstructor
public class AdminPlacesPageController {

    private static final int PAGE_SIZE = 20;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm 'UTC'")
                    .withZone(ZoneOffset.UTC);

    private final PlaceService placeService;
    private final AdminPlaceManagementService managementService;
    private final CategoryService categoryService;
    private final FeatureService featureService;

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "ALL") String active,
            @RequestParam(defaultValue = "ALL") String sourceType,
            @RequestParam(defaultValue = "") String category,
            Model model
    ) {
        String cleanSearch = search == null ? "" : search.strip();
        if (cleanSearch.length() > 100) {
            cleanSearch = cleanSearch.substring(0, 100);
        }
        Boolean activeFilter = switch (active) {
            case "true" -> true;
            case "false" -> false;
            default -> null;
        };
        PlaceSourceType sourceFilter = switch (sourceType) {
            case "MANUAL" -> PlaceSourceType.MANUAL;
            case "OSM" -> PlaceSourceType.OSM;
            default -> null;
        };
        String categoryFilter = categoryService.findAllActive().stream()
                .anyMatch(c -> c.getCode().equals(category)) ? category : "";

        int requestedPage = Math.max(page, 0);
        PageResponse<PlaceListItemResponse> result = placeService.findAll(
                requestedPage, PAGE_SIZE, activeFilter, sourceFilter,
                categoryFilter, cleanSearch
        );
        if (result.totalPages() > 0 && requestedPage >= result.totalPages()) {
            result = placeService.findAll(
                    result.totalPages() - 1, PAGE_SIZE,
                    activeFilter, sourceFilter, categoryFilter, cleanSearch
            );
        }
        int currentPage = result.totalPages() == 0 ? 0 : result.page();
        int first = Math.max(0, currentPage - 2);
        int last = Math.min(result.totalPages() - 1, currentPage + 2);
        List<Integer> pageNumbers = result.totalPages() == 0 ? List.of()
                : IntStream.rangeClosed(first, last).boxed().toList();

        model.addAttribute("pageTitle", "Туристические объекты");
        model.addAttribute("activePage", "places");
        model.addAttribute("places", result.content().stream()
                .map(p -> new PlaceRow(p.getId(), p.getName(), p.getAddress(),
                        p.getSourceType(), p.isActive(), p.getLatitude(),
                        p.getLongitude(), format(p.getUpdatedAt())))
                .toList());
        model.addAttribute("totalElements", result.totalElements());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("page", currentPage);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("search", cleanSearch);
        model.addAttribute("selectedActive", activeFilter == null ? "ALL" : activeFilter.toString());
        model.addAttribute("selectedSource", sourceFilter == null ? "ALL" : sourceFilter.name());
        model.addAttribute("selectedCategory", categoryFilter);
        model.addAttribute("categories", categoryService.findAllActive());
        return "admin/places/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("placeForm", new AdminPlaceForm());
        populateFormModel(model, false, null);
        return "admin/places/form";
    }

    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute("placeForm") AdminPlaceForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes redirect
    ) {
        if (errors.hasErrors()) {
            populateFormModel(model, false, null);
            return "admin/places/form";
        }
        try {
            Long id = managementService.create(form);
            redirect.addFlashAttribute("success", "Туристический объект создан.");
            return "redirect:/admin/places/" + id;
        } catch (UnknownPlaceCategoryException | UnknownFeatureException ex) {
            errors.reject("reference.invalid", "Некоторые категории или характеристики недоступны. Проверьте выбранные значения.");
            populateFormModel(model, false, null);
            return "admin/places/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        PlaceResponse place = placeService.getById(id);
        model.addAttribute("pageTitle", "Карточка объекта");
        model.addAttribute("activePage", "places");
        model.addAttribute("place", place);
        model.addAttribute("createdAt", format(place.getCreatedAt()));
        model.addAttribute("updatedAt", format(place.getUpdatedAt()));
        model.addAttribute("featureCatalog", featureService.findAllActive());
        return "admin/places/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PlaceResponse place = placeService.getById(id);
        model.addAttribute("placeForm", AdminPlaceForm.from(place));
        populateFormModel(model, true, id);
        return "admin/places/form";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("placeForm") AdminPlaceForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes redirect
    ) {
        if (errors.hasErrors()) {
            populateFormModel(model, true, id);
            return "admin/places/form";
        }
        try {
            managementService.update(id, form);
            redirect.addFlashAttribute("success", "Изменения сохранены.");
            return "redirect:/admin/places/" + id;
        } catch (UnknownPlaceCategoryException | UnknownFeatureException ex) {
            errors.reject("reference.invalid", "Некоторые категории или характеристики недоступны. Проверьте выбранные значения.");
            populateFormModel(model, true, id);
            return "admin/places/form";
        }
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirect) {
        placeService.deactivate(id);
        redirect.addFlashAttribute("success", "Объект отключён, запись сохранена в базе.");
        return "redirect:/admin/places/" + id;
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes redirect) {
        placeService.activate(id);
        redirect.addFlashAttribute("success", "Объект снова активен.");
        return "redirect:/admin/places/" + id;
    }

    @ExceptionHandler(PlaceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(Model model) {
        model.addAttribute("pageTitle", "Объект не найден");
        model.addAttribute("activePage", "places");
        return "admin/places/not-found";
    }

    private void populateFormModel(Model model, boolean editing, Long id) {
        model.addAttribute("pageTitle", editing ? "Редактирование объекта" : "Новый объект");
        model.addAttribute("activePage", "places");
        model.addAttribute("editing", editing);
        model.addAttribute("placeId", id);
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("featureCatalog", featureService.findAllActive());
        model.addAttribute("featureLevels", List.of(0, 1, 2, 3, 4, 5));
    }

    private static String format(Instant instant) {
        return instant == null ? "—" : DATE_FORMAT.format(instant);
    }

    public record PlaceRow(Long id, String name, String address,
                           String sourceType, boolean active, Double latitude,
                           Double longitude, String updatedAt) {}
}
