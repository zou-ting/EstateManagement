package com.autogen.propmgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "facility")
public class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "facility_no", nullable = false, unique = true, length = 20)
    private String facilityNo;

    @Column(name = "facility_name", nullable = false, length = 50)
    private String facilityName;

    @Column(name = "facility_type", length = 30)
    private String facilityType;

    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    @Column(length = 100)
    private String location;

    @Column
    private Integer status = 1;

    @Column(name = "install_date")
    private LocalDate installDate;

    @Column(name = "warranty_end")
    private LocalDate warrantyEnd;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}