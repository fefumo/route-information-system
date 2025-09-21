package se.ifmo.route_information_system.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import se.ifmo.route_information_system.model.Coordinates;
import se.ifmo.route_information_system.model.Location;
import se.ifmo.route_information_system.model.Route;
import se.ifmo.route_information_system.repository.LocationRepository;
import se.ifmo.route_information_system.repository.RouteRepository;

@Service
@Transactional
public class RouteService {
    private final RouteRepository routes;
    private final LocationRepository locations;

    public RouteService(RouteRepository routes, LocationRepository locations) {
        this.routes = routes;
        this.locations = locations;
    }

    public int deleteByRating(float rating) {
        return (int) routes.deleteByRating(rating);
    }

    public Route findShortest(Long fromId, Long toId) {
        return routes.findFirstByFrom_IdAndTo_IdOrderByDistanceAsc(fromId, toId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No route found from " + fromId + " to " + toId));

    }

    public Route findLongest(Long fromId, Long toId) {
        return routes.findFirstByFrom_IdAndTo_IdOrderByDistanceDesc(fromId, toId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No route found from " + fromId + " to " + toId));
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

}
