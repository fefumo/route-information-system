package se.ifmo.route_information_system.events;

import se.ifmo.route_information_system.dto.RouteRowDto;

public record RouteChangedEvent(String action, RouteRowDto route) {
    // action: "CREATED" | "UPDATED" | "DELETED"
}
