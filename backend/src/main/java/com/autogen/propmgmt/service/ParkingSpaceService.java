package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.entity.ParkingSpace;
import com.autogen.propmgmt.repository.ParkingSpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingSpaceService {

    private final ParkingSpaceRepository parkingSpaceRepository;

    // 列表查询（支持按楼栋和状态筛选）
    public List<ParkingSpace> list(Long buildingId, Integer status) {
        if (buildingId != null) {
            return parkingSpaceRepository.findByBuildingId(buildingId);
        }
        if (status != null) {
            return parkingSpaceRepository.findByStatus(status);
        }
        return parkingSpaceRepository.findAll();
    }

    // 按业主查询（业主端使用）
    public List<ParkingSpace> listByOwner(Long ownerId) {
        return parkingSpaceRepository.findByOwnerId(ownerId);
    }

    // 获取详情
    public ParkingSpace getById(Long id) {
        return parkingSpaceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("车位不存在"));
    }

    // 新增/更新
    public ParkingSpace save(ParkingSpace space) {
        if (!StringUtils.hasText(space.getSpaceNo())) {
            throw new BusinessException("车位编号不能为空");
        }
        if (space.getBuildingId() == null) {
            throw new BusinessException("所属楼栋不能为空");
        }
        return parkingSpaceRepository.save(space);
    }

    // 分配车位给业主
    public ParkingSpace assignToOwner(Long spaceId, Long ownerId) {
        ParkingSpace space = getById(spaceId);
        if (space.getStatus() == 2) {
            throw new BusinessException("该车位已被占用");
        }
        space.setOwnerId(ownerId);
        space.setStatus(2);  // 占用
        return parkingSpaceRepository.save(space);
    }

    // 释放车位
    public ParkingSpace releaseOwner(Long spaceId) {
        ParkingSpace space = getById(spaceId);
        space.setOwnerId(null);
        space.setStatus(1);  // 空闲
        return parkingSpaceRepository.save(space);
    }

    // 删除
    public void delete(Long id) {
        parkingSpaceRepository.deleteById(id);
    }
}