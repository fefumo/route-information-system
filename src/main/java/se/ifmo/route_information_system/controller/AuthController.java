package se.ifmo.route_information_system.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import se.ifmo.route_information_system.dto.RegistrationForm;
import se.ifmo.route_information_system.service.UserService;

@Controller
public class AuthController {

    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegistrationForm("", "", "", ""));
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String doRegister(@ModelAttribute("form") @Valid RegistrationForm form,
            BindingResult binding,
            Model model) {
        if (binding.hasErrors()) {
            return "auth/register";
        }
        try {
            users.register(form);
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "auth/register";
        }
        return "redirect:/login?registered";
    }
}
