package com.blooddonor.blooddonor.repository;

import com.blooddonor.blooddonor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    long countByRole(String role);
    Optional<User> findByEmail(String email);
    List<User> findByRole(String role);
    @Query("SELECT u FROM User u " +
           "WHERE u.role = 'DONOR' " +
           "AND u.available = true " +
           "AND (:bloodGroup IS NULL OR u.bloodGroup = :bloodGroup) " +
           "AND (:location IS NULL OR " +
           "LOWER(u.location) LIKE LOWER(CONCAT('%', :location, '%')))")
    List<User> searchAvailableDonors(
            @Param("bloodGroup") String bloodGroup,
            @Param("location") String location);
}