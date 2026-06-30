package com.autogen.propmgmt.repository;

import com.autogen.propmgmt.entity.HousePurchaseApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HousePurchaseApplicationRepository extends JpaRepository<HousePurchaseApplication, Long> {
    List<HousePurchaseApplication> findByOwnerId(Long ownerId);
    List<HousePurchaseApplication> findByOwnerIdAndStatus(Long ownerId, String status);
    List<HousePurchaseApplication> findByUnitId(Long unitId);
    List<HousePurchaseApplication> findByStatus(String status);
}
