package com.autogen.propmgmt.repository;

import com.autogen.propmgmt.entity.ParkingSpace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {
    Optional<ParkingSpace> findBySpaceNo(String spaceNo);
    List<ParkingSpace> findByBuildingId(Long buildingId);
    List<ParkingSpace> findByStatus(Integer status);
    List<ParkingSpace> findByOwnerId(Long ownerId);
}