package se.ifmo.route_information_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import se.ifmo.route_information_system.repository.RouteRepository;
import se.ifmo.route_information_system.service.RouteService;

@Controller
@RequestMapping("/special")
public class SpecialOperationsController {

    private final RouteService service;
    private final RouteRepository routeRepo; // only if you still list results here

    public SpecialOperationsController(RouteService service,
            RouteRepository routeRepo) {
        this.service = service;
        this.routeRepo = routeRepo;
    }

    @GetMapping
    public String specialHome() {
        return "special"; // special.html
    }

    @PostMapping("/deleteByRating")
    public String deleteByRating(@RequestParam float rating, RedirectAttributes attrs) {
        try {
            int deleted = service.deleteByRating(rating);
            attrs.addFlashAttribute("message", deleted + " route(s) deleted");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            attrs.addFlashAttribute("error",
                    "Cannot delete: some routes are referenced by other objects.");
        }
        return "redirect:/special";
    }

    @GetMapping("/findByName")
    public String findByName(@RequestParam String substring, Model model) {
        model.addAttribute("resultsByName", routeRepo.findByNameContaining(substring));
        return "special";
    }

    @GetMapping("/findByRatingLess")
    public String findByRatingLess(@RequestParam float value, Model model) {
        model.addAttribute("resultsByRatingLess", routeRepo.findByRatingLessThan(value));
        return "special";
    }

    @GetMapping("/shortest")
    public String shortest(@RequestParam Long fromId, @RequestParam Long toId, Model model) {
        model.addAttribute("extremeRoute", service.findShortest(fromId, toId));
        model.addAttribute("extremeType", "shortest");
        return "special";
    }

    @GetMapping("/longest")
    public String longest(@RequestParam Long fromId, @RequestParam Long toId, Model model) {
        model.addAttribute("extremeRoute", service.findLongest(fromId, toId));
        model.addAttribute("extremeType", "longest");
        return "special";
    }

    @PostMapping("/addBetween")
    public String addBetween(@RequestParam(required = false) Long fromId,
            @RequestParam Long toId,
            @RequestParam String name,
            @RequestParam Float coordX,
            @RequestParam Double coordY,
            @RequestParam int distance,
            @RequestParam float rating,
            RedirectAttributes attrs) {
        var saved = service.addBetween(fromId, toId, name, coordX, coordY, distance, rating);
        attrs.addFlashAttribute("message", "Route #" + saved.getId() + " added.");
        return "redirect:/special";
    }
}
