package se.ifmo.route_information_system.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import se.ifmo.route_information_system.model.Coordinates;
import se.ifmo.route_information_system.model.Route;
import se.ifmo.route_information_system.repository.LocationRepository;
import se.ifmo.route_information_system.repository.RouteRepository;
import se.ifmo.route_information_system.service.RouteSpecs;

@Controller
@RequestMapping("/routes")
public class RouteMvcController {

    private final RouteRepository routeRepo;
    private final LocationRepository locationRepo;

    public RouteMvcController(RouteRepository routeRepo, LocationRepository locationRepo) {
        this.routeRepo = routeRepo;
        this.locationRepo = locationRepo;
    }

    // MAIN PAGE: GET /routes (renders templates/list.html)
    @GetMapping
    public String list(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long fromId,
            @RequestParam(required = false) Long toId,
            @RequestParam(required = false, defaultValue = "id,asc") String sort) {

        String[] s = sort.split(",", 2);
        Sort.Direction dir = (s.length == 2 && "desc".equalsIgnoreCase(s[1])) ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, s[0]));

        Specification<Route> spec = (root, q, cb) -> cb.conjunction();
        spec = spec.and(RouteSpecs.nameEq(name))
                .and(RouteSpecs.fromIdEq(fromId))
                .and(RouteSpecs.toIdEq(toId));

        Page<Route> routes = routeRepo.findAll(spec, pageable);

        model.addAttribute("routes", routes);
        model.addAttribute("locations", locationRepo.findAll());

        model.addAttribute("name", name);
        model.addAttribute("fromId", fromId);
        model.addAttribute("toId", toId);

        model.addAttribute("sort", sort);

        return "list";
    }

    // SHOW CREATE FORM: GET /routes/new (renders templates/form.html)
    @GetMapping("/new")
    public String newRoute(Model model) {
        var r = new Route();
        if (r.getCoordinates() == null)
            r.setCoordinates(new Coordinates());
        model.addAttribute("route", r);
        model.addAttribute("locations", locationRepo.findAll());
        model.addAttribute("formTitle", "Create Route");
        model.addAttribute("action", "/routes");
        return "form";
    }

    // HANDLE CREATE: POST /routes
    @PostMapping
    public String create(@ModelAttribute("route") @Valid Route route,
            BindingResult binding,
            Model model,
            RedirectAttributes attrs) {

        var from = route.getFrom();
        var to = route.getTo();
        Long fromId = (from != null ? from.getId() : null);
        Long toId = (to != null ? to.getId() : null);

        if (fromId != null && toId != null && java.util.Objects.equals(fromId, toId)) {
            // attach to a field so Thymeleaf shows it near the select
            binding.rejectValue("to", "route.sameLocations",
                    "From and To locations must be different");
        }

        if (binding.hasErrors()) {
            model.addAttribute("locations", locationRepo.findAll());
            model.addAttribute("formTitle", "Create Route");
            model.addAttribute("action", "/routes");
            return "form"; // return the view (no redirect) so errors are kept
        }

        routeRepo.save(route);
        attrs.addFlashAttribute("message", "Route created.");
        return "redirect:/routes";
    }

    // VIEW ONE: GET /routes/{id} (renders templates/detail.html)
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Route route = routeRepo.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));
        model.addAttribute("route", route);
        return "detail";
    }

    // SHOW EDIT FORM: GET /routes/{id}/edit (reuses form.html)
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var r = routeRepo.findById(id).orElseThrow();
        if (r.getCoordinates() == null)
            r.setCoordinates(new Coordinates());
        model.addAttribute("route", r);
        model.addAttribute("locations", locationRepo.findAll());
        model.addAttribute("formTitle", "Edit Route");
        model.addAttribute("action", "/routes/" + id);
        return "form";
    }

    // HANDLE UPDATE: POST /routes/{id}
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
            @ModelAttribute("route") @Valid Route route,
            BindingResult binding,
            Model model,
            RedirectAttributes attrs) {
        if (binding.hasErrors()) {
            model.addAttribute("locations", locationRepo.findAll());
            model.addAttribute("formTitle", "Edit Route");
            model.addAttribute("action", "/routes/" + id);
            return "form";
        }
        route.setId(id);
        routeRepo.save(route);
        attrs.addFlashAttribute("message", "Route updated.");
        return "redirect:/routes/" + id;
    }

    // HANDLE DELETE: POST /routes/{id}/delete
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes attrs) {
        try {
            routeRepo.deleteById(id);
            attrs.addFlashAttribute("message", "Route deleted.");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            attrs.addFlashAttribute("error",
                    "Cannot delete: the route is referenced by other objects.");
        }
        return "redirect:/routes";
    }
}
