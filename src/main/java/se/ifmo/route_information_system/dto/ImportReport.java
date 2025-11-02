package se.ifmo.route_information_system.dto;

import java.util.List;

public record ImportReport(
        int received,
        List<String> imported,
        List<String> errors) {
}
