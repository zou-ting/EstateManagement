package com.autogen.propmgmt.repository;

import com.autogen.propmgmt.entity.VisitorRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitorRecordRepository extends JpaRepository<VisitorRecord, Long> {
    List<VisitorRecord> findAll();

    // ✅ 按状态查询
    List<VisitorRecord> findByStatus(String status);

    // ✅ 按业主查询
    List<VisitorRecord> findByOwnerId(Long ownerId);
}