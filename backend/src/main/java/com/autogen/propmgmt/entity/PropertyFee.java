package com.autogen.propmgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "property_fee")
public class PropertyFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false, length = 7)
    private String billMonth;

    @Column
    private BigDecimal managementFee;

    @Column
    private BigDecimal publicFee;

    @Column
    private BigDecimal totalFee;

    @Column(length = 20)
    private String payStatus = "UNPAID";

    @Column(length = 20)
    private String feeType = "PROPERTY";  // PROPERTY=物业费, HOUSE_PURCHASE=购房款

    @Column(name = "house_purchase_id")
    private Long housePurchaseId;  // 关联的购房申请ID

    @Column(name = "parking_purchase_id")
    private Long parkingPurchaseId;  // 关联的车位购买申请ID

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
