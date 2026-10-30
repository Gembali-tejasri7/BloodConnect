package com.blooddonor.blooddonor.controller;

import com.blooddonor.blooddonor.entity.User;
import com.blooddonor.blooddonor.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DonorSearchController {

    private final UserRepository userRepository;

    public DonorSearchController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/find-donors")
    public String findDonors(
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String location,
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        User currentUser = userRepository.findById(userId).orElse(null);

        if (currentUser == null || !"RECIPIENT".equals(currentUser.getRole())) {
            return "redirect:/dashboard";
        }

        // Convert empty search fields to null.
        if (bloodGroup != null && bloodGroup.isBlank()) {
            bloodGroup = null;
        }

        if (location != null && location.isBlank()) {
            location = null;
        }

        List<User> donors = userRepository.searchAvailableDonors(
                bloodGroup,
                location == null ? null : location.trim()
        );

        model.addAttribute("donors", donors);
        model.addAttribute("selectedBloodGroup",
                bloodGroup == null ? "" : bloodGroup);
        model.addAttribute("selectedLocation",
                location == null ? "" : location);

        return "find-donors";
    }
}