package com.blooddonor.blooddonor.controller;

import com.blooddonor.blooddonor.entity.BloodRequest;
import com.blooddonor.blooddonor.entity.User;
import com.blooddonor.blooddonor.repository.BloodRequestRepository;
import com.blooddonor.blooddonor.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Controller
public class BloodRequestController {

    private final BloodRequestRepository bloodRequestRepository;
    private final UserRepository userRepository;

    private static final Set<String> BLOOD_GROUPS = Set.of(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    );

    public BloodRequestController(
            BloodRequestRepository bloodRequestRepository,
            UserRepository userRepository) {
        this.bloodRequestRepository = bloodRequestRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/blood-request/new")
    public String showRequestForm(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        User user = userRepository.findById(userId).orElse(null);

        if (user == null || !"RECIPIENT".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        return "blood-request";
    }

    @PostMapping("/blood-request/new")
    public String submitRequest(
            @RequestParam String bloodGroup,
            @RequestParam String location,
            @RequestParam Integer units,
            @RequestParam(required = false) String description,
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        User recipient = userRepository.findById(userId).orElse(null);

        if (recipient == null || !"RECIPIENT".equals(recipient.getRole())) {
            return "redirect:/dashboard";
        }

        if (!BLOOD_GROUPS.contains(bloodGroup)
                || location == null || location.isBlank()
                || units == null || units < 1 || units > 10
                || (description != null && description.length() > 500)) {

            model.addAttribute("error",
                    "Please enter valid request details. Units must be between 1 and 10.");
            return "blood-request";
        }

        BloodRequest request = new BloodRequest();

        request.setRecipient(recipient);
        request.setBloodGroup(bloodGroup);
        request.setLocation(location.trim());
        request.setUnits(units);
        request.setDescription(description == null ? "" : description.trim());
        request.setStatus("PENDING");

        bloodRequestRepository.save(request);

        return "redirect:/blood-requests/my";
    }

    @GetMapping("/blood-requests/my")
    public String showMyRequests(HttpSession session, Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        User recipient = userRepository.findById(userId).orElse(null);

        if (recipient == null || !"RECIPIENT".equals(recipient.getRole())) {
            return "redirect:/dashboard";
        }

        List<BloodRequest> requests =
                bloodRequestRepository.findByRecipientOrderByRequestDateDesc(recipient);

        model.addAttribute("requests", requests);

        return "my-blood-requests";
    }
}