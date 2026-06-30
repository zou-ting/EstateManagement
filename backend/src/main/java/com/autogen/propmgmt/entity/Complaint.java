package com.autogen.propmgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "complaint")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 20)
    private String type = "COMPLAINT";

    @Column(length = 20)
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String reply;

    private LocalDateTime replyDate;

    private LocalDateTime createdAt;

    @Transient
    private Integer replyCount = 0;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (type == null) type = "COMPLAINT";
        if (status == null) status = "PENDING";
        if (replyCount == null) replyCount = 0;
    }
}