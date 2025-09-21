package se.ifmo.route_information_system.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import se.ifmo.route_information_system.model.Coordinates;
import se.ifmo.route_information_system.model.Route;
import se.ifmo.route_information_system.service.RouteService;

@Controller
@RequestMapping("/routes")
public class RouteMvcController {

    private final RouteService routeService;

    public RouteMvcController(RouteService routeService) {
        this.routeService = routeService;
    }

    // MAIN PAGE
    @GetMapping
    public String list(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long fromId,
            @RequestParam(required = false) Long toId,
            @RequestParam(required = false, defaultValue = "id,asc") String sort) {

        Page<Route> routes = routeService.findRoutes(page, size, sort, name, fromId, toId);

        model.addAttribute("routes", routes);
        model.addAttribute("locations", routeService.findAllLocations());

        // echo current filters back to the view so links keep them
        model.addAttribute("name", name);
        model.addAttribute("fromId", fromId);
        model.addAttribute("toId", toId);
        model.addAttribute("sort", sort);

        return "list";
    }

    // SHOW CREATE FORM
    @GetMapping("/new")
    public String newRoute(Model model) {
        var r = new Route();
        if (r.getCoordinates() == null)
            r.setCoordinates(new Coordinates());

        model.addAttribute("route", r);
        model.addAttribute("locations", routeService.findAllLocations());
        model.addAttribute("formTitle", "Create Route");
        model.addAttribute("action", "/routes");
        return "form";
    }

    // HANDLE CREATE
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
            binding.rejectValue("to", "route.sameLocations", "From and To locations must be different");
        }

        if (binding.hasErrors()) {
            model.addAttribute("locations", routeService.findAllLocations());
            model.addAttribute("formTitle", "Create Route");
            model.addAttribute("action", "/routes");
            return "form";
        }

        routeService.create(route);
        attrs.addFlashAttribute("message", "Route created.");
        return "redirect:/routes";
    }

    // VIEW ONE
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("route", routeService.getOr404(id));
        return "detail";
    }

    // SHOW EDIT FORM
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var r = routeService.getOr404(id);
        if (r.getCoordinates() == null)
            r.setCoordinates(new Coordinates());

        model.addAttribute("route", r);
        model.addAttribute("locations", routeService.findAllLocations());
        model.addAttribute("formTitle", "Edit Route");
        model.addAttribute("action", "/routes/" + id);
        return "form";
    }

    // HANDLE UPDATE
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
            @ModelAttribute("route") @Valid Route route,
            BindingResult binding,
            Model model,
            RedirectAttributes attrs) {
        if (binding.hasErrors()) {
            model.addAttribute("locations", routeService.findAllLocations());
            model.addAttribute("formTitle", "Edit Route");
            model.addAttribute("action", "/routes/" + id);
            return "form";
        }
        routeService.update(id, route);
        attrs.addFlashAttribute("message", "Route updated.");
        return "redirect:/routes/" + id;
    }

    // HANDLE DELETE
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes attrs) {
        try {
            routeService.delete(id);
            attrs.addFlashAttribute("message", "Route deleted.");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            attrs.addFlashAttribute("error", "Cannot delete: the route is referenced by other objects.");
        }
        return "redirect:/routes";
    }
}
