package se.ifmo.route_information_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.ifmo.route_information_system.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
