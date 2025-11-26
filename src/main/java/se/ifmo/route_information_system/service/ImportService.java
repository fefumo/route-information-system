package se.ifmo.route_information_system.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import se.ifmo.route_information_system.dto.ImportReport;
import se.ifmo.route_information_system.dto.RouteImportDto;
import se.ifmo.route_information_system.model.Coordinates;
import se.ifmo.route_information_system.model.ImportOperation;
import se.ifmo.route_information_system.model.ImportStatus;
import se.ifmo.route_information_system.model.Location;
import se.ifmo.route_information_system.model.Route;
import se.ifmo.route_information_system.repository.ImportRepository;
import se.ifmo.route_information_system.repository.LocationRepository;
import se.ifmo.route_information_system.repository.RouteRepository;

@Service
@Transactional
public class ImportService {
    private final RouteRepository routes;
    private final LocationRepository locations;
    private final UserService users;
    private final ImportTx importTx;
    private final Validator validator;
    private final FileStorageService storage;

    public ImportService(RouteRepository routes,
            LocationRepository locations,
            ImportRepository importOps,
            UserService users,
            ImportTx importTx,
            Validator validator,
            FileStorageService storage) {
        this.routes = routes;
        this.locations = locations;
        this.users = users;
        this.importTx = importTx;
        this.validator = validator;
        this.storage = storage;
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
                    .filter(l -> dto.fromName().equals(l.getName()))
                    .findFirst()
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "fromId not found: " + dto.fromId()));
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

    public ImportReport importRoutes(List<RouteImportDto> items) {
        var currentUser = users.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        ImportOperation op = new ImportOperation();
        op.setStatus(ImportStatus.RUNNING);
        op.setStartedBy(currentUser);
        op.setStartedAt(Instant.now());
        op = importTx.saveNew(op); // REQUIRES_NEW

        return doImport(op, items);
    }

    public ImportReport importRoutesForOperation(ImportOperation existingOp,
            List<RouteImportDto> items) {
        if (existingOp.getId() == null) {
            existingOp = importTx.saveNew(existingOp);
        }
        return doImport(existingOp, items);
    }

    private ImportReport doImport(ImportOperation op, List<RouteImportDto> items) {
        List<String> errors = new ArrayList<>();
        List<String> imported = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            RouteImportDto item = items.get(i);

            var violations = validator.validate(item);
            if (!violations.isEmpty()) {
                String msg = violations.stream()
                        .limit(3)
                        .map(v -> v.getPropertyPath() + " " + v.getMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Validation error");
                errors.add("Item #" + (i + 1) + ": " + msg);
                continue;
            }

            try {
                Route r = toEntity(item);
                routes.save(r);
                imported.add(r.getName());
            } catch (jakarta.validation.ConstraintViolationException ve) {
                String msg = ve.getConstraintViolations().stream()
                        .limit(3)
                        .map(v -> v.getPropertyPath() + " " + v.getMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Constraint violation");
                errors.add("Item #" + (i + 1) + ": " + msg);
            } catch (Exception e) {
                errors.add("Item #" + (i + 1) + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            String joined = String.join(" | ", errors);

            storage.deleteQuietly(op.getSourceObjectKey());

            importTx.finish(op, ImportStatus.FAILED, null,
                    joined.substring(0, Math.min(1000, joined.length())));

            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Import aborted: " + errors.size() + " error(s)");
        }

        importTx.finish(op, ImportStatus.SUCCESS, imported.size(), null);
        return new ImportReport(items.size(), imported, List.of());
    }
}
