package com.blooddonor.blooddonor.controller;

import com.blooddonor.blooddonor.entity.User;
import com.blooddonor.blooddonor.repository.UserRepository;
import com.blooddonor.blooddonor.repository.BloodRequestRepository;
import com.blooddonor.blooddonor.repository.BloodRequestResponseRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final BloodRequestRepository requestRepository;
    private final BloodRequestResponseRepository responseRepository;

    public AdminController(
            UserRepository userRepository,
            BloodRequestRepository requestRepository,
            BloodRequestResponseRepository responseRepository) {

        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.responseRepository = responseRepository;
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {

        // Check whether the user is logged in
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        // Get the logged-in user's details
        User admin = userRepository.findById(userId).orElse(null);

        // Allow access only to ADMIN
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/dashboard";
        }

        // Dashboard statistics
        model.addAttribute("totalUsers",
                userRepository.count());

        model.addAttribute("totalDonors",
                userRepository.countByRole("DONOR"));

        model.addAttribute("totalRecipients",
                userRepository.countByRole("RECIPIENT"));

        model.addAttribute("totalRequests",
                requestRepository.count());

        model.addAttribute("totalResponses",
                responseRepository.count());

        // Retrieve all users
        model.addAttribute("users",
                userRepository.findAll());

        // Retrieve blood requests in descending date order
        model.addAttribute("requests",
                requestRepository.findAllByOrderByRequestDateDesc());

        // Retrieve donor responses in descending date order
        model.addAttribute("responses",
                responseRepository.findAllByOrderByResponseDateDesc());

        model.addAttribute("donors", userRepository.findByRole("DONOR"));

        return "admin-dashboard";
    }
    @PostMapping("/admin/donors/{id}/availability")
    public String updateDonorAvailability(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        User admin = userRepository.findById(userId).orElse(null);

        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/dashboard";
        }

        User donor = userRepository.findById(id).orElse(null);

        if (donor == null || !"DONOR".equals(donor.getRole())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Donor not found.");
            return "redirect:/admin/dashboard";
        }

        donor.setAvailable(!Boolean.TRUE.equals(donor.getAvailable()));
        userRepository.save(donor);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Donor availability updated successfully.");

        return "redirect:/admin/dashboard";
    }
    @PostMapping("/admin/requests/{id}/status")
    public String updateRequestStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        User admin = userRepository.findById(userId).orElse(null);

        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/dashboard";
        }

        if (!status.equals("PENDING")
                && !status.equals("APPROVED")
                && !status.equals("COMPLETED")
                && !status.equals("CANCELLED")) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Invalid request status.");

            return "redirect:/admin/dashboard";
        }

        var bloodRequest = requestRepository.findById(id).orElse(null);

        if (bloodRequest == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Blood request not found.");

            return "redirect:/admin/dashboard";
        }

        bloodRequest.setStatus(status);
        requestRepository.save(bloodRequest);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Blood request status updated successfully.");

        return "redirect:/admin/dashboard";
    }
}
