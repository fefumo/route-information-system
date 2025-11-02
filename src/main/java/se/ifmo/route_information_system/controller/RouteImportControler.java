package se.ifmo.route_information_system.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import se.ifmo.route_information_system.dto.ImportReport;
import se.ifmo.route_information_system.dto.RouteImportDto;
import se.ifmo.route_information_system.service.ImportService;

@RestController
@RequestMapping("/routes")
@Validated
public class RouteImportControler {

    private final ImportService importService;

    public RouteImportControler(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping(path = "/import", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ImportReport importRoutes(@RequestBody @Valid List<@Valid RouteImportDto> items) {
        return importService.importRoutes(items);
    }
}
