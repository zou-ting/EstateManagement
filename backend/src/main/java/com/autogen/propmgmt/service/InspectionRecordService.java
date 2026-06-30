package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.entity.InspectionRecord;
import com.autogen.propmgmt.entity.PropUnit;
import com.autogen.propmgmt.repository.InspectionRecordRepository;
import com.autogen.propmgmt.repository.PropUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InspectionRecordService {

    private final InspectionRecordRepository inspectionRecordRepository;
    private final PropUnitRepository unitRepository;  // ✅ 新增注入

    public List<InspectionRecord> list() {
        return inspectionRecordRepository.findAll();
    }

    public InspectionRecord getById(Long id) {
        return inspectionRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("巡检记录不存在"));
    }

    public InspectionRecord save(InspectionRecord entity) {
        // ✅ 如果 roomId 有值但 buildingId 为空，自动填充
        if (entity.getRoomId() != null && entity.getBuildingId() == null) {
            unitRepository.findById(entity.getRoomId())
                    .ifPresent(unit -> entity.setBuildingId(unit.getBuildingId()));
        }
        return inspectionRecordRepository.save(entity);
    }

    public void delete(Long id) {
        inspectionRecordRepository.deleteById(id);
    }
}