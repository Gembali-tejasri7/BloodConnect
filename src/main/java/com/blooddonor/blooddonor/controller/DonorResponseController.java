package com.blooddonor.blooddonor.controller;

import com.blooddonor.blooddonor.entity.BloodRequest;
import com.blooddonor.blooddonor.entity.BloodRequestResponse;
import com.blooddonor.blooddonor.entity.User;

import com.blooddonor.blooddonor.repository.BloodRequestRepository;
import com.blooddonor.blooddonor.repository.BloodRequestResponseRepository;
import com.blooddonor.blooddonor.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class DonorResponseController {

    private final BloodRequestRepository requestRepository;
    private final BloodRequestResponseRepository responseRepository;
    private final UserRepository userRepository;

    public DonorResponseController(
            BloodRequestRepository requestRepository,
            BloodRequestResponseRepository responseRepository,
            UserRepository userRepository) {

        this.requestRepository = requestRepository;
        this.responseRepository = responseRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/donor/requests")
    public String showRequests(HttpSession session, Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        User donor = userRepository.findById(userId).orElse(null);

        if (donor == null || !"DONOR".equals(donor.getRole())) {
            return "redirect:/dashboard";
        }

        if (!Boolean.TRUE.equals(donor.getAvailable())) {
            model.addAttribute("unavailable", true);
            return "donor-requests";
        }

        List<BloodRequest> requests =
                requestRepository.findByStatusOrderByRequestDateDesc("PENDING");

        requests.removeIf(request ->
                !donor.getBloodGroup().equals(request.getBloodGroup())
                || donor.getLocation() == null
                || !donor.getLocation().equalsIgnoreCase(request.getLocation())
        );

        model.addAttribute("requests", requests);
        model.addAttribute("unavailable", false);

        return "donor-requests";
    }

    @PostMapping("/donor/requests/{requestId}/respond")
    public String respondToRequest(
            @PathVariable Long requestId,
            @RequestParam String response,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        User donor = userRepository.findById(userId).orElse(null);

        if (donor == null
                || !"DONOR".equals(donor.getRole())
                || !Boolean.TRUE.equals(donor.getAvailable())) {
            return "redirect:/dashboard";
        }

        if (!response.equals("ACCEPTED")
                && !response.equals("DECLINED")) {
            return "redirect:/donor/requests";
        }

        BloodRequest request =
                requestRepository.findById(requestId).orElse(null);

        if (request == null
                || !"PENDING".equals(request.getStatus())
                || !donor.getBloodGroup().equals(request.getBloodGroup())
                || donor.getLocation() == null
                || !donor.getLocation().equalsIgnoreCase(request.getLocation())) {
            return "redirect:/donor/requests";
        }

        BloodRequestResponse donorResponse =
                responseRepository.findByBloodRequestAndDonor(request, donor)
                        .orElseGet(BloodRequestResponse::new);

        donorResponse.setBloodRequest(request);
        donorResponse.setDonor(donor);
        donorResponse.setStatus(response);

        responseRepository.save(donorResponse);

        return "redirect:/donor/requests";
    }

    @GetMapping("/blood-requests/{requestId}/responses")
    public String showResponses(
            @PathVariable Long requestId,
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

        BloodRequest request =
                requestRepository.findById(requestId).orElse(null);

        if (request == null
                || !request.getRecipient().getId().equals(recipient.getId())) {
            return "redirect:/blood-requests/my";
        }

        List<BloodRequestResponse> responses =
                responseRepository.findByBloodRequestOrderByResponseDateDesc(
                        request);

        model.addAttribute("request", request);
        model.addAttribute("responses", responses);

        return "request-responses";
    }
}