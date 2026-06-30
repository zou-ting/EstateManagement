package com.autogen.propmgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "decoration_record")
public class DecorationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "decoration_type", length = 30)
    private String decorationType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 20)
    private String status = "PENDING";

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(length = 50)
    private String contractor;

    @Column(length = 50)
    private String supervisor;

    @Column(length = 200)
    private String remark;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}