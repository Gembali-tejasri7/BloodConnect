
package com.blooddonor.blooddonor.controller;

import com.blooddonor.blooddonor.entity.User;
import com.blooddonor.blooddonor.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistrationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {

        model.addAttribute("user", new User());

        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute User user,
            Model model) {

        // Validate the user's name
        if (user.getFullName() == null ||
                user.getFullName().trim().isEmpty()) {

            model.addAttribute("error", "Name is required.");
            return "register";
        }

        // Validate email
        if (user.getEmail() == null ||
                user.getEmail().trim().isEmpty()) {

            model.addAttribute("error", "Email is required.");
            return "register";
        }

        // Normalize email
        user.setEmail(user.getEmail().trim().toLowerCase());

        // Check duplicate email
        if (userRepository.existsByEmail(user.getEmail())) {

            model.addAttribute("error",
                    "This email is already registered.");

            return "register";
        }

        // Allow only DONOR and RECIPIENT roles
        if (!"DONOR".equals(user.getRole()) &&
                !"RECIPIENT".equals(user.getRole())) {

            model.addAttribute("error",
                    "Invalid role selected.");

            return "register";
        }

        // Validate password
        if (user.getPassword() == null ||
                user.getPassword().length() < 8) {

            model.addAttribute("error",
                    "Password must contain at least 8 characters.");

            return "register";
        }

        // Hash password using BCrypt
        user.setPassword(
                passwordEncoder.encode(user.getPassword()));

        // Save user
        userRepository.save(user);

        model.addAttribute("success",
                "Registration successful! You can now log in.");

        model.addAttribute("user", new User());

        return "register";
    }
}
