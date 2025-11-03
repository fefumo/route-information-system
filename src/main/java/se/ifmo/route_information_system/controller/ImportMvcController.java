package se.ifmo.route_information_system.controller;

import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import se.ifmo.route_information_system.model.ImportOperation;
import se.ifmo.route_information_system.repository.ImportRepository;
import se.ifmo.route_information_system.service.UserService;

@Controller
@RequestMapping("/imports")
public class ImportMvcController {

    private final ImportRepository importRepo;
    private final UserService users;

    public ImportMvcController(ImportRepository importRepo, UserService users) {
        this.importRepo = importRepo;
        this.users = users;
    }

    @GetMapping
    public String list(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        boolean isAdmin = isAdmin();
        Page<ImportOperation> ops = isAdmin
                ? importRepo.findAllByOrderByIdDesc(pageable)
                : importRepo.findByStartedByOrderByIdDesc(
                        users.getCurrentUser().orElseThrow(), pageable);

        model.addAttribute("ops", ops);
        model.addAttribute("isAdmin", isAdmin);
        return "imports";
    }

    private boolean isAdmin() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream()
                .anyMatch(ga -> "ROLE_ADMIN".equals(ga.getAuthority()));
    }
}
