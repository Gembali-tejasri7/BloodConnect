package com.blooddonor.blooddonor.repository;

import com.blooddonor.blooddonor.entity.BloodRequest;
import com.blooddonor.blooddonor.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BloodRequestRepository
        extends JpaRepository<BloodRequest, Long> {

    List<BloodRequest> findByRecipientOrderByRequestDateDesc(User recipient);

    List<BloodRequest> findByStatusOrderByRequestDateDesc(String status);
    List<BloodRequest> findAllByOrderByRequestDateDesc();
}