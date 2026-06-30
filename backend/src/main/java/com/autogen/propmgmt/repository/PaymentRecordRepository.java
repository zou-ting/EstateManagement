package com.autogen.propmgmt.repository;

import com.autogen.propmgmt.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    List<PaymentRecord> findByRoomId(Long roomId);
    List<PaymentRecord> findByFeeId(Long feeId);
    List<PaymentRecord> findByRoomIdOrderByPayDateDesc(Long roomId);
}