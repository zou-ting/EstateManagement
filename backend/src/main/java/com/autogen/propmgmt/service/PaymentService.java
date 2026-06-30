package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.entity.PaymentRecord;
import com.autogen.propmgmt.entity.PropertyFee;
import com.autogen.propmgmt.repository.PaymentRecordRepository;
import com.autogen.propmgmt.repository.PropertyFeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final PropertyFeeRepository propertyFeeRepository;

    // 获取所有缴费记录
    public List<PaymentRecord> list() {
        return paymentRecordRepository.findAll();
    }

    // 按房间查询（返回实体）
    public List<PaymentRecord> listByRoom(Long roomId) {
        return paymentRecordRepository.findByRoomId(roomId);
    }

    // 按ID查询
    public PaymentRecord getById(Long id) {
        return paymentRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("缴费记录不存在"));
    }

    // 在线支付
    @Transactional
    public PaymentRecord pay(Long feeId, String payMethod, String operator) {
        PropertyFee fee = propertyFeeRepository.findById(feeId)
                .orElseThrow(() -> new BusinessException("账单不存在"));

        if ("PAID".equals(fee.getPayStatus())) {
            throw new BusinessException("该账单已支付");
        }

        PaymentRecord record = new PaymentRecord();
        record.setFeeId(feeId);
        record.setRoomId(fee.getRoomId());
        record.setAmount(fee.getTotalFee() != null ? fee.getTotalFee() : BigDecimal.ZERO);
        record.setPayMethod(payMethod);
        record.setPayDate(LocalDateTime.now());
        record.setOperator(operator);

        PaymentRecord saved = paymentRecordRepository.save(record);

        fee.setPayStatus("PAID");
        propertyFeeRepository.save(fee);

        return saved;
    }

    // 新增
    @Transactional
    public PaymentRecord save(PaymentRecord record) {
        return paymentRecordRepository.save(record);
    }

    // 删除
    public void delete(Long id) {
        paymentRecordRepository.deleteById(id);
    }
}