package se.ifmo.route_information_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "import_operations")
@Data
public class ImportOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ImportStatus status = ImportStatus.RUNNING;

    @ManyToOne(optional = false)
    @JoinColumn(name = "started_by_id", nullable = false)
    private User startedBy;

    @Column(nullable = false, updatable = false)
    private Instant startedAt = Instant.now();

    /** Устанавливается при SUCCESS или FAILED */
    private Instant finishedAt;

    /** Заполняется только при SUCCESS */
    private Integer addedCount;

    /** Опционально: текст ошибки при FAILED (по желанию) */
    @Column(length = 2000)
    private String errorMessage;
}
