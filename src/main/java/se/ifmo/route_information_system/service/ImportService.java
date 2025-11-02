package se.ifmo.route_information_system.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import se.ifmo.route_information_system.dto.ImportReport;
import se.ifmo.route_information_system.dto.RouteImportDto;
import se.ifmo.route_information_system.model.Coordinates;
import se.ifmo.route_information_system.model.Location;
import se.ifmo.route_information_system.model.Route;
import se.ifmo.route_information_system.repository.LocationRepository;
import se.ifmo.route_information_system.repository.RouteRepository;

@Service
@Transactional
public class ImportService {
    private final RouteRepository routes;
    private final LocationRepository locations;

    public ImportService(RouteRepository routes, LocationRepository locations) {
        this.routes = routes;
        this.locations = locations;
    }

    private Route toEntity(RouteImportDto dto) {
        // Validate minimal target
        if (dto.toId() == null && (dto.toName() == null || dto.toName().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toId or toName must be provided");
        }
        // to
        Location to = null;
        if (dto.toId() != null) {
            to = locations.findById(dto.toId()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "toId not found: " + dto.toId()));
        } else {
            to = locations.findAll().stream()
                    .filter(l -> dto.toName().equals(l.getName()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "toName not found: " + dto.toName()));
        }

        // from
        Location from = null;
        if (dto.fromId() != null) {
            from = locations.findById(dto.fromId()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromId no found: " + dto.fromId()));
        } else {
            from = locations.findAll().stream()
                    .filter(l -> dto.toName().equals(l.getName()))
                    .findFirst()
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "fromId no found: " + dto.fromId()));
        }

        // Build Route
        if (from.getId() != null && to.getId() != null && from.getId().equals(to.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "From and To locations must be different, fromId: " + from.getId() + " toId: " + to.getId());
        }

        if (dto.coordinates() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "coordinates is required");
        }

        Coordinates c = new Coordinates();
        // You used float/double in entity; cast carefully:
        c.setX((float) dto.coordinates().x());
        c.setY((double) dto.coordinates().y());

        Route r = new Route();
        r.setName(dto.name());
        r.setCoordinates(c);
        r.setFrom(from);
        r.setTo(to);
        r.setDistance(dto.distance());
        r.setRating(dto.rating());
        return r;

    }

    @Transactional
    public ImportReport importRoutes(@Valid List<RouteImportDto> items) {
        List<String> errors = new ArrayList<>();
        List<String> imported = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            RouteImportDto dto = items.get(i);
            try {
                Route r = toEntity(dto);
                routes.save(r);
                imported.add(r.getName());
            } catch (Exception e) {
                errors.add("Item #" + (i + 1) + ":" + e.getMessage());
            }
        }
        return new ImportReport(items.size(), imported, errors);
    }
}
