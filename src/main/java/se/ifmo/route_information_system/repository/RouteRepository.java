package se.ifmo.route_information_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import se.ifmo.route_information_system.model.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {
    // Spring creates all the CRUD code automatically
}
