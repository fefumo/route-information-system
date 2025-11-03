package se.ifmo.route_information_system.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import se.ifmo.route_information_system.model.ImportOperation;
import se.ifmo.route_information_system.model.User;

public interface ImportRepository extends JpaRepository<ImportOperation, Long> {

    Page<ImportOperation> findByStartedByOrderByIdDesc(User startedBy, Pageable pageable);

    Page<ImportOperation> findAllByOrderByIdDesc(Pageable pageable);
}
