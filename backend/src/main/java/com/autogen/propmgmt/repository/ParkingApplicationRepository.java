package com.autogen.propmgmt.repository;

import com.autogen.propmgmt.entity.ParkingApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParkingApplicationRepository extends JpaRepository<ParkingApplication, Long> {
    List<ParkingApplication> findByOwnerId(Long ownerId);
    List<ParkingApplication> findByOwnerIdAndStatus(Long ownerId, String status);
    List<ParkingApplication> findByParkingSpaceId(Long parkingSpaceId);
    List<ParkingApplication> findByStatus(String status);
}