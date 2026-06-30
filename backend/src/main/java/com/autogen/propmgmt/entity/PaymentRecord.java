package com.autogen.propmgmt.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_record")
public class PaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long feeId;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 20)
    private String payMethod = "CASH";

    @Column(nullable = false)
    private LocalDateTime payDate;

    @Column(length = 50)
    private String operator;

    @Column(length = 200)
    private String remark;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (payDate == null) {
            payDate = LocalDateTime.now();
        }
        if (payMethod == null) {
            payMethod = "CASH";
        }
    }
}