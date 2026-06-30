package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.entity.VisitorRecord;
import com.autogen.propmgmt.repository.VisitorRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitorRecordService {

    private final VisitorRecordRepository visitorRecordRepository;

    // ✅ 支持按状态筛选
    public List<VisitorRecord> list(String status) {
        if (StringUtils.hasText(status)) {
            return visitorRecordRepository.findByStatus(status);
        }
        return visitorRecordRepository.findAll();
    }

    // ✅ 按业主查询
    public List<VisitorRecord> listByOwner(Long ownerId) {
        return visitorRecordRepository.findByOwnerId(ownerId);
    }

    public VisitorRecord getById(Long id) {
        return visitorRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("来访登记不存在"));
    }

    public VisitorRecord save(VisitorRecord entity) {
        if (!StringUtils.hasText(entity.getVisitorName())) {
            throw new BusinessException("访客姓名不能为空");
        }
        return visitorRecordRepository.save(entity);
    }

    public void delete(Long id) {
        visitorRecordRepository.deleteById(id);
    }
}