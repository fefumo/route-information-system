package se.ifmo.route_information_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationForm(
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 72) String password,
        @NotBlank String confirmPassword) {
}
