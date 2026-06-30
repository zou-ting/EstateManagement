package com.autogen.propmgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "complaint_reply")
public class ComplaintReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_id", nullable = false)
    private Long complaintId;

    @Column(name = "reply_content", nullable = false, columnDefinition = "TEXT")
    private String replyContent;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "admin_read")
    private Boolean adminRead = false;

    @Column(name = "owner_read")
    private Boolean ownerRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}