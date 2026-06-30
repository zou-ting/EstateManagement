package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.entity.Complaint;
import com.autogen.propmgmt.repository.ComplaintRepository;
import com.autogen.propmgmt.repository.ComplaintReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintReplyRepository replyRepository;

    // 管理端列表（支持状态和类型筛选 + 回复数量）
    public List<Complaint> list(String status, String type) {
        List<Complaint> list;
        if (status != null && !status.isBlank()) {
            list = complaintRepository.findByStatus(status);
        } else if (type != null && !type.isBlank()) {
            list = complaintRepository.findByType(type);
        } else {
            list = complaintRepository.findAll();
        }
        // 填充回复数量
        for (Complaint c : list) {
            int count = replyRepository.countByComplaintId(c.getId());
            c.setReplyCount(count);
        }
        return list;
    }

    // 业主端查询（按业主）
    public List<Complaint> listByOwner(Long ownerId) {
        return complaintRepository.findByOwnerId(ownerId);
    }

    // 获取详情
    public Complaint getById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new BusinessException("投诉建议不存在"));
    }

    // 新增/更新
    public Complaint save(Complaint complaint) {
        if (complaint.getCreatedAt() == null) {
            complaint.setCreatedAt(LocalDateTime.now());
        }
        return complaintRepository.save(complaint);
    }

    // 更新状态
    public Complaint updateStatus(Long id, String status) {
        Complaint complaint = getById(id);
        complaint.setStatus(status);
        return complaintRepository.save(complaint);
    }

    // 删除
    public void delete(Long id) {
        complaintRepository.deleteById(id);
    }
}