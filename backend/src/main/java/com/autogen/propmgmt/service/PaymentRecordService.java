package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.dto.PaymentRecordDetail;
import com.autogen.propmgmt.entity.PaymentRecord;
import com.autogen.propmgmt.repository.PaymentRecordRepository;
import com.autogen.propmgmt.repository.PropertyFeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentRecordService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final PropertyFeeRepository propertyFeeRepository;

    // 按房间查询（返回详情DTO，带账单月份）
    public List<PaymentRecordDetail> findDetailByRoomId(Long roomId) {
        List<PaymentRecord> records = paymentRecordRepository.findByRoomIdOrderByPayDateDesc(roomId);
        List<PaymentRecordDetail> details = new ArrayList<>();
        for (PaymentRecord record : records) {
            PaymentRecordDetail dto = new PaymentRecordDetail();
            dto.setId(record.getId());
            dto.setFeeId(record.getFeeId());
            dto.setRoomId(record.getRoomId());
            dto.setAmount(record.getAmount());
            dto.setPayMethod(record.getPayMethod());
            dto.setPayDate(record.getPayDate());
            dto.setOperator(record.getOperator());
            dto.setRemark(record.getRemark());

            if (record.getFeeId() != null) {
                propertyFeeRepository.findById(record.getFeeId())
                        .ifPresent(fee -> dto.setBillMonth(fee.getBillMonth()));
            }
            details.add(dto);
        }
        return details;
    }

    // 按账单ID查询
    public List<PaymentRecord> findByFeeId(Long feeId) {
        return paymentRecordRepository.findByFeeId(feeId);
    }

    // 新增缴费记录
    public PaymentRecord save(PaymentRecord record) {
        return paymentRecordRepository.save(record);
    }

    // 按账单ID更新缴费记录
    @Transactional
    public PaymentRecord updateByFeeId(Long feeId, PaymentRecord record) {
        List<PaymentRecord> existing = paymentRecordRepository.findByFeeId(feeId);
        if (existing.isEmpty()) {
            return paymentRecordRepository.save(record);
        }
        PaymentRecord target = existing.get(0);
        target.setAmount(record.getAmount());
        target.setPayMethod(record.getPayMethod());
        target.setOperator(record.getOperator());
        target.setPayDate(java.time.LocalDateTime.now());
        if (record.getRemark() != null) {
            target.setRemark(record.getRemark());
        }
        return paymentRecordRepository.save(target);
    }

    // 按账单ID删除缴费记录
    @Transactional
    public void deleteByFeeId(Long feeId) {
        List<PaymentRecord> existing = paymentRecordRepository.findByFeeId(feeId);
        if (!existing.isEmpty()) {
            paymentRecordRepository.deleteAll(existing);
        }
    }
}