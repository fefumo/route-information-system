package se.ifmo.route_information_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import se.ifmo.route_information_system.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {

}
