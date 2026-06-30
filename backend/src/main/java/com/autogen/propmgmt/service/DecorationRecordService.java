package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.entity.DecorationRecord;
import com.autogen.propmgmt.repository.DecorationRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DecorationRecordService {

    private final DecorationRecordRepository repository;

    public List<DecorationRecord> list() {
        return repository.findAll();
    }

    public DecorationRecord getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("装修记录不存在"));
    }

    public DecorationRecord save(DecorationRecord entity) {
        return repository.save(entity);
    }

    public DecorationRecord approve(Long id) {
        DecorationRecord record = getById(id);
        record.setStatus("APPROVED");
        record.setApprovalDate(LocalDate.now());
        return repository.save(record);
    }

    public DecorationRecord reject(Long id) {
        DecorationRecord record = getById(id);
        record.setStatus("REJECTED");
        return repository.save(record);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    // ✅ 按业主ID查询装修记录（业主端使用）
    public List<DecorationRecord> listByOwner(Long ownerId) {
        return repository.findAll().stream()
                .filter(r -> ownerId.equals(r.getOwnerId()))
                .collect(Collectors.toList());
    }
}