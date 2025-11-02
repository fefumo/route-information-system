package se.ifmo.route_information_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record RouteImportDto(

        @NotBlank String name,
        CoordinatesDto coordinates,
        Long fromId,
        String fromName,
        Long toId,
        String toName,
        @Min(2) int distance,
        @Positive float rating) {
    public record CoordinatesDto(double x, double y) {
    }
}
