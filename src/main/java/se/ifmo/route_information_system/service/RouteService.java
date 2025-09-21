package se.ifmo.route_information_system.service;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import se.ifmo.route_information_system.model.Coordinates;
import se.ifmo.route_information_system.model.Location;
import se.ifmo.route_information_system.model.Route;
import se.ifmo.route_information_system.repository.LocationRepository;
import se.ifmo.route_information_system.repository.RouteRepository;

@Service
@Transactional
public class RouteService {

    private static final Set<String> ALLOWED_SORTS = Set.of("id", "name", "distance", "rating", "creationDate");

    private final RouteRepository routes;
    private final LocationRepository locations;

    public RouteService(RouteRepository routes, LocationRepository locations) {
        this.routes = routes;
        this.locations = locations;
    }

    @Transactional(readOnly = true)
    public Page<Route> findRoutes(
            int page,
            int size,
            String sort, // "id,asc"
            String name,
            Long fromId,
            Long toId) {
        Sort springSort = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, springSort);

        Specification<Route> spec = Specification.allOf(
                RouteSpecs.nameEq(name), // returns null when name is null/blank
                RouteSpecs.fromIdEq(fromId), // returns null when fromId is null
                RouteSpecs.toIdEq(toId) // returns null when toId is null
        );

        return routes.findAll(spec, pageable);
    }

    private Sort parseSort(String sort) {
        String property = "id";
        Sort.Direction dir = Sort.Direction.ASC;

        if (sort != null && !sort.isBlank()) {
            String[] s = sort.split(",", 2);
            String candidate = s[0].trim();
            if (ALLOWED_SORTS.contains(candidate)) {
                property = candidate;
            }
            if (s.length == 2 && "desc".equalsIgnoreCase(s[1])) {
                dir = Sort.Direction.DESC;
            }
        }
        return Sort.by(dir, property);
    }

    @Transactional(readOnly = true)
    public List<Location> findAllLocations() {
        return locations.findAll();
    }

    // TODO: create a normal 404 maybe?
    @Transactional(readOnly = true)
    public Route getOr404(Long id) {
        return routes.findById(id).orElseThrow(
                () -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));
    }

    public Route create(@Valid Route route) {
        return routes.save(route);
    }

    public Route addBetween(Long fromId, Long toId, String name,
            Float coordX, Double coordY,
            int distance, float rating) {
        Location from = (fromId == null) ? null
                : locations.findById(fromId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromId not found"));
        Location to = locations.findById(toId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "toId not found"));

        Route r = new Route();
        r.setName(name);
        Coordinates c = new Coordinates();
        c.setX(coordX);
        c.setY(coordY);
        r.setCoordinates(c);
        r.setFrom(from);
        r.setTo(to);
        r.setDistance(distance);
        r.setRating(rating);
        return routes.save(r);
    }

    public Route update(Long id, @Valid Route route) {
        if (!routes.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        route.setId(id);
        return routes.save(route);
    }

    public void delete(Long id) {
        routes.deleteById(id);
    }

    public boolean deleteOneByRating(float rating) {
        return routes.findFirstByRating(rating)
                .map(r -> {
                    routes.deleteById(r.getId());
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<Route> findByNameSubstring(String substring) {
        if (substring == null || substring.isBlank())
            return List.of();
        return routes.findByNameContainingIgnoreCase(substring.trim());
    }

    @Transactional(readOnly = true)
    public List<Route> findByRatingLess(float value) {
        return routes.findByRatingLessThan(value);
    }

    @Transactional(readOnly = true)
    public Route findShortest(Long fromId, Long toId) {
        return routes.findFirstByFrom_IdAndTo_IdOrderByDistanceAsc(fromId, toId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No route found from " + fromId + " to " + toId));
    }

    @Transactional(readOnly = true)
    public Route findLongest(Long fromId, Long toId) {
        return routes.findFirstByFrom_IdAndTo_IdOrderByDistanceDesc(fromId, toId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No route found from " + fromId + " to " + toId));
    }

    public Route addBetween(@Nullable Long fromId,
            Long toId,
            String name,
            double coordX,
            int coordY,
            int distance,
            float rating) {

        Location to = locations.findById(toId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "toId not found: " + toId));

        Location from;
        if (fromId != null) {
            from = locations.findById(fromId)
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromId not found: " + fromId));
        } else {
            // create a new Location for provided coordinates
            from = new Location();
            from.setName("L(" + coordX + "," + coordY + ")"); // or any naming rule you like
            from.setX(coordX);
            from.setY(coordY);
            from = locations.save(from);
        }

        Route r = new Route();
        r.setName(name);

        Coordinates c = new Coordinates();
        c.setX((int) coordX);
        c.setY(coordY);
        r.setCoordinates(c);

        r.setFrom(from);
        r.setTo(to);
        r.setDistance(distance);
        r.setRating(rating);
        return routes.save(r);
    }
}
