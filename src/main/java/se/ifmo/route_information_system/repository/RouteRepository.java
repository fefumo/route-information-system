package se.ifmo.route_information_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.ifmo.route_information_system.model.Route;

// Spring creates all the CRUD code automatically
public interface RouteRepository extends JpaRepository<Route, Long>, JpaSpecificationExecutor<Route> {

    long deleteByRating(float rating);

    List<Route> findByNameContaining(String substring);

    List<Route> findByRatingLessThan(float value);

    // find longest
    Optional<Route> findFirstByFrom_IdAndTo_IdOrderByDistanceAsc(Long fromId, Long toId);

    /*
     *
     * equivalent to:
     * 
     * @Query(value = """
     * select *
     * from routes r
     * where r.from_location_id = :fromId and r.to_location_id = :toId
     * order by r.distance desc
     * limit 1
     * """, nativeQuery = true)
     *
     */

    // find shortest
    Optional<Route> findFirstByFrom_IdAndTo_IdOrderByDistanceDesc(Long fromId, Long toId);
}
