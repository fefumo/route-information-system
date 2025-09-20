package se.ifmo.route_information_system.controller;

import java.util.List;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import se.ifmo.route_information_system.model.Route;
import se.ifmo.route_information_system.repository.RouteRepository;

@RestController
@RequestMapping("api/routes")
public class RouteController {
    private final RouteRepository repo;

    public RouteController(RouteRepository repo) {
        this.repo = repo;
    }

    // GET api/routes
    @GetMapping
    public List<Route> getAll(Pageable pageable) { // pagination support
        return repo.findAll();
    }

    // POST api/routes, body: JSON representing a route
    @PostMapping
    public Route create(@Valid @RequestBody Route route) { // validation support
        return repo.save(route);
    }

}
