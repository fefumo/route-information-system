package se.ifmo.route_information_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 72)
    private String passwordHash;

    @Column(nullable = false, length = 32)
    private String role = "ROLE_USER";

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
