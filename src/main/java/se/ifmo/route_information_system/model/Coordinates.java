package se.ifmo.route_information_system.model;

import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class Coordinates {
    @Max(421)
    private float x;
    @Max(375)
    private double y;
}
