package com.autogen.propmgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "parking_application")
public class ParkingApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parking_space_id", nullable = false)
    private Long parkingSpaceId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "application_type", nullable = false, length = 20)
    private String applicationType; // RENT/RENEW/CANCEL/TRANSFER

    @Column(name = "plate_number", length = 20)
    private String plateNumber;

    @Column(name = "vehicle_type", length = 20)
    private String vehicleType;

    @Column(name = "rental_duration", length = 20)
    private String rentalDuration; // MONTHLY/QUARTERLY/YEARLY

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "transfer_to_owner_id")
    private Long transferToOwnerId;

    @Column(length = 200)
    private String reason;

    @Column(name = "purchase_amount", precision = 12, scale = 2)
    private BigDecimal purchaseAmount;

    @Column(length = 20)
    private String status = "PENDING"; // PENDING/APPROVED/REJECTED/CANCELLED/DONE

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