package se.ifmo.route_information_system.model;

import jakarta.validation.constraints.Max;
import lombok.Data;

/**
 * Coordinates
 */
@Data
public class Coordinates {
    @Max(421)
    private float x; // max: 421
    @Max(375)
    private double y; // max: 375
}
