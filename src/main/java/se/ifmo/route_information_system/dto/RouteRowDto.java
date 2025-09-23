package se.ifmo.route_information_system.dto;

public record RouteRowDto(
        Long id,
        String name,
        Integer coordX,
        Integer coordY,
        Long fromId,
        String fromName,
        Long toId,
        String toName,
        Integer distance,
        Float rating,
        String creationDate // ISO-8601 string
) {
}
