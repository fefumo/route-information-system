package se.ifmo.route_information_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import se.ifmo.route_information_system.model.Route;
import se.ifmo.route_information_system.service.RouteService;

@Controller
@RequestMapping("/special")
public class SpecialOperationsController {

    private final RouteService routeService;

    public SpecialOperationsController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public String page() {
        return "special";
    }

    @PostMapping("/deleteByRating")
    public String deleteByRating(@RequestParam float rating, RedirectAttributes ra) {
        boolean deleted = routeService.deleteOneByRating(rating);
        if (deleted)
            ra.addFlashAttribute("message", "Deleted one route with rating " + rating);
        else
            ra.addFlashAttribute("error", "No route with rating " + rating);
        return "redirect:/special";
    }

    @GetMapping("/findByName")
    public String findByName(@RequestParam String substring, Model model) {
        model.addAttribute("resultsByName", routeService.findByNameSubstring(substring));
        return "special";
    }

    @GetMapping("/findByRatingLess")
    public String findByRatingLess(@RequestParam("value") float value, Model model) {
        model.addAttribute("resultsByRatingLess", routeService.findByRatingLess(value));
        return "special";
    }

    @GetMapping("/shortest")
    public String shortest(@RequestParam Long fromId, @RequestParam Long toId, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("extremeType", "Shortest");
            model.addAttribute("extremeRoute", routeService.findShortest(fromId, toId));
            return "special";
        } catch (ResponseStatusException e) {
            ra.addFlashAttribute("error", e.getReason());
            return "redirect:/special";
        }
    }

    @GetMapping("/longest")
    public String longest(@RequestParam Long fromId, @RequestParam Long toId, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("extremeType", "Longest");
            model.addAttribute("extremeRoute", routeService.findLongest(fromId, toId));
            return "special";
        } catch (ResponseStatusException e) {
            ra.addFlashAttribute("error", e.getReason());
            return "redirect:/special";
        }
    }

    @PostMapping("/addBetween")
    public String addBetween(
            @RequestParam(required = false) Long fromId,
            @RequestParam Long toId,
            @RequestParam String name,
            @RequestParam double coordX,
            @RequestParam int coordY,
            @RequestParam int distance,
            @RequestParam float rating,
            RedirectAttributes ra) {

        try {
            Route saved = routeService.addBetween(fromId, toId, name, coordX, coordY, distance, rating);
            ra.addFlashAttribute("message", "Added route #" + saved.getId() + " (“" + saved.getName() + "”)");
        } catch (ResponseStatusException e) {
            ra.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/special";
    }
}
