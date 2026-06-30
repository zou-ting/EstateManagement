package com.autogen.propmgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "house_purchase_application")
public class HousePurchaseApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    @Column(name = "unit_no", length = 20)
    private String unitNo;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "purchase_amount")
    private Double purchaseAmount;

    @Column(length = 200)
    private String remark;

    @Column(length = 20)
    private String status = "PENDING";

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "approval_remark", length = 200)
    private String approvalRemark;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
