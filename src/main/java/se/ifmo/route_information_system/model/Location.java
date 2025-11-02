package se.ifmo.route_information_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double x;
    private int y;

    @NotNull
    @Column(nullable = false, unique = true)
    private String name;
}
