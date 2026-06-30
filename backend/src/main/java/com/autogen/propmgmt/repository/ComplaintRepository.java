package com.autogen.propmgmt.repository;

import com.autogen.propmgmt.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    // 按业主查询（业主端使用）
    List<Complaint> findByOwnerId(Long ownerId);

    // 按状态筛选（管理端使用）
    List<Complaint> findByStatus(String status);

    // 按类型筛选（管理端使用）
    List<Complaint> findByType(String type);
}