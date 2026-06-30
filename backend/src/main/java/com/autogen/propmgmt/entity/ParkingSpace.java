package com.autogen.propmgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "parking_space")
public class ParkingSpace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "space_no", nullable = false, unique = true, length = 20)
    private String spaceNo;

    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    @Column(length = 20)
    private String type = "FIXED";

    @Column
    private Integer status = 1;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "area_sqm", precision = 8, scale = 2)
    private BigDecimal areaSqm;

    @Column(name = "monthly_fee", precision = 10, scale = 2)
    private BigDecimal monthlyFee;

    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    private LocalDateTime createdAt;

    @Column(name = "plate_number", length = 20)
    private String plateNumber;

    @Column(name = "vehicle_type", length = 20)
    private String vehicleType;

    @Column(name = "rent_start_date")
    private LocalDate rentStartDate;

    @Column(name = "rent_end_date")
    private LocalDate rentEndDate;

    @Column(name = "rental_duration", length = 20)
    private String rentalDuration;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}