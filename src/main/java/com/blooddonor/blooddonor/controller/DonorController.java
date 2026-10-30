
package com.blooddonor.blooddonor.controller;

import com.blooddonor.blooddonor.entity.User;
import com.blooddonor.blooddonor.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DonorController {

    private final UserRepository userRepository;

    public DonorController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Display donor profile
    @GetMapping("/donor/profile")
    public String showProfile(
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        User user = userRepository.findById(userId).orElse(null);

        if (user == null || !"DONOR".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);

        return "donor-profile";
    }

    // Update donor profile
    @PostMapping("/donor/profile")
    public String updateProfile(
            @ModelAttribute User formUser,
            @RequestParam(defaultValue = "false") Boolean available,
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        User user = userRepository.findById(userId).orElse(null);

        if (user == null || !"DONOR".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        if (formUser.getBloodGroup() == null ||
                formUser.getBloodGroup().isBlank() ||
                formUser.getLocation() == null ||
                formUser.getLocation().isBlank()) {

            model.addAttribute("error",
                    "Please enter your blood group and location.");

            model.addAttribute("user", user);

            return "donor-profile";
        }

        user.setBloodGroup(formUser.getBloodGroup());
        user.setLocation(formUser.getLocation().trim());
        user.setAvailable(available);

        userRepository.save(user);

        model.addAttribute("user", user);
        model.addAttribute("success",
                "Your donor profile has been updated successfully!");

        return "donor-profile";
    }
}
