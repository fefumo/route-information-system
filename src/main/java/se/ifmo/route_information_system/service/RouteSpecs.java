package se.ifmo.route_information_system.service;

import org.springframework.data.jpa.domain.Specification;

import se.ifmo.route_information_system.model.Route;

public final class RouteSpecs {
    private RouteSpecs() {
    }

    public static Specification<Route> nameEq(String name) {
        return (root, q, cb) -> (name == null || name.isBlank())
                ? cb.conjunction()
                : cb.equal(root.get("name"), name.trim());
    }

    public static Specification<Route> fromIdEq(Long fromId) {
        return (root, q, cb) -> (fromId == null)
                ? cb.conjunction()
                : cb.equal(root.join("from").get("id"), fromId);
    }

    public static Specification<Route> toIdEq(Long toId) {
        return (root, q, cb) -> (toId == null)
                ? cb.conjunction()
                : cb.equal(root.join("to").get("id"), toId);
    }
}
