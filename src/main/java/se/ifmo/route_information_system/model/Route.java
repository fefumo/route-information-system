package se.ifmo.route_information_system.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.Date;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Data
@Entity
@Table(name = "routes")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotEmpty(message = "Please provide a name")
    private String name;

    @Embedded
    @Valid
    private Coordinates coordinates;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false, updatable = false)
    private Date creationDate = new Date();

    @ManyToOne
    @JoinColumn(name = "from_location_id")
    private Location from; // may be null WHYYYYY

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_location_id")
    @NotNull
    private Location to;

    @Min(2) // must be > 1
    private int distance;

    @Positive
    private float rating;
}
