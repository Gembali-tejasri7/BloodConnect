package com.blooddonor.blooddonor.repository;

import com.blooddonor.blooddonor.entity.BloodRequest;
import com.blooddonor.blooddonor.entity.BloodRequestResponse;
import com.blooddonor.blooddonor.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BloodRequestResponseRepository
        extends JpaRepository<BloodRequestResponse, Long> {

    List<BloodRequestResponse> findByDonorOrderByResponseDateDesc(
            User donor);

    List<BloodRequestResponse> findByBloodRequestOrderByResponseDateDesc(
            BloodRequest bloodRequest);

    List<BloodRequestResponse> findAllByOrderByResponseDateDesc();

    Optional<BloodRequestResponse> findByBloodRequestAndDonor(
            BloodRequest bloodRequest, User donor);
}