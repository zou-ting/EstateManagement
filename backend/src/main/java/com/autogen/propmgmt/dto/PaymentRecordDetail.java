package com.autogen.propmgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRecordDetail {
    private Long id;
    private Long feeId;
    private Long roomId;
    private String billMonth;
    private BigDecimal amount;
    private String payMethod;
    private LocalDateTime payDate;
    private String operator;
    private String remark;
}