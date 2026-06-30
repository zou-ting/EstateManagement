package com.autogen.propmgmt.repository;

import com.autogen.propmgmt.entity.DecorationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DecorationRecordRepository extends JpaRepository<DecorationRecord, Long> {
    List<DecorationRecord> findAll();
}