package com.autogen.propmgmt.repository;

import com.autogen.propmgmt.entity.ComplaintReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintReplyRepository extends JpaRepository<ComplaintReply, Long> {
    // 按投诉ID查询回复（按创建时间升序）
    List<ComplaintReply> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);

    // 统计回复数量
    int countByComplaintId(Long complaintId);

    // 统计管理员未读回复数量
    int countByAdminReadFalse();

    // 统计业主未读回复数量
    int countByOwnerReadFalseAndOwnerId(Long ownerId);

    // 按投诉ID统计管理员未读数量
    int countByComplaintIdAndAdminReadFalse(Long complaintId);

    // 按投诉ID统计业主未读数量
    int countByComplaintIdAndOwnerReadFalseAndOwnerId(Long complaintId, Long ownerId);
}