package se.ifmo.route_information_system.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import se.ifmo.route_information_system.dto.RegistrationForm;
import se.ifmo.route_information_system.model.User;
import se.ifmo.route_information_system.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public User register(@Valid RegistrationForm f) {
        if (!f.password().equals(f.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }
        if (repo.existsByUsername(f.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        if (repo.existsByEmail(f.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already taken");
        }

        User u = new User();
        u.setUsername(f.username());
        u.setEmail(f.email());
        u.setPasswordHash(encoder.encode(f.password()));
        u.setRole("ROLE_USER");
        return repo.save(u);
    }

    @Transactional(readOnly = true)
    public Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return repo.findByUsername(auth.getName());
    }
}
