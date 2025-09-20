package se.ifmo.route_information_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @Embedded
    private Coordinates coordinates;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false, updatable = false)
    private Date creationDate = new Date();

    @ManyToOne
    @JoinColumn(name = "from_location_id")
    private Location from; // may be null

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_location_id")
    private Location to;

    @Min(2) // must be > 1
    private int distance;

    @Positive
    private float rating;
}
