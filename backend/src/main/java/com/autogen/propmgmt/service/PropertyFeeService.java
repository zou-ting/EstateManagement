package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.entity.HousePurchaseApplication;
import com.autogen.propmgmt.entity.Owner;
import com.autogen.propmgmt.entity.ParkingApplication;
import com.autogen.propmgmt.entity.PropertyFee;
import com.autogen.propmgmt.entity.PropUnit;
import com.autogen.propmgmt.repository.HousePurchaseApplicationRepository;
import com.autogen.propmgmt.repository.OwnerRepository;
import com.autogen.propmgmt.repository.ParkingApplicationRepository;
import com.autogen.propmgmt.repository.PropertyFeeRepository;
import com.autogen.propmgmt.repository.PropUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyFeeService {

    private final PropertyFeeRepository feeRepository;
    private final PropUnitRepository unitRepository;
    private final HousePurchaseApplicationRepository housePurchaseRepository;
    private final ParkingApplicationRepository parkingApplicationRepository;
    private final OwnerRepository ownerRepository;

    public List<PropertyFee> list() {
        return feeRepository.findAll();
    }

    // ✅ 按房间查询
    public List<PropertyFee> listByRoom(Long roomId) {
        return feeRepository.findAll().stream()
                .filter(f -> f.getRoomId().equals(roomId))
                .collect(Collectors.toList());
    }

    // ✅ 按状态查询
    public List<PropertyFee> listByStatus(String status) {
        return feeRepository.findAll().stream()
                .filter(f -> status.equals(f.getPayStatus()))
                .collect(Collectors.toList());
    }

    public PropertyFee getById(Long id) {
        return feeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("物业费账单不存在"));
    }

    public PropertyFee save(PropertyFee entity) {
        BigDecimal mgmt = entity.getManagementFee() != null ? entity.getManagementFee() : BigDecimal.ZERO;
        BigDecimal pub = entity.getPublicFee() != null ? entity.getPublicFee() : BigDecimal.ZERO;
        entity.setTotalFee(mgmt.add(pub));
        return feeRepository.save(entity);
    }

    public void delete(Long id) {
        feeRepository.deleteById(id);
    }

    // ✅ 批量生成账单
    @Transactional
    public List<PropertyFee> generateBatch(String billMonth, BigDecimal baseRate, BigDecimal publicRate) {
        YearMonth month = YearMonth.parse(billMonth, DateTimeFormatter.ofPattern("yyyy-MM"));

        List<PropUnit> units = unitRepository.findAll();
        List<PropertyFee> createdFees = new ArrayList<>();

        for (PropUnit unit : units) {
            // 跳过没有业主的房屋
            if (unit.getOwnerCount() == null || unit.getOwnerCount() == 0) {
                continue;
            }

            BigDecimal area = unit.getAreaSqm() != null ? unit.getAreaSqm() : BigDecimal.valueOf(80);
            BigDecimal managementFee = area.multiply(baseRate);
            BigDecimal publicFee = area.multiply(publicRate);
            BigDecimal totalFee = managementFee.add(publicFee);

            PropertyFee fee = new PropertyFee();
            fee.setRoomId(unit.getId());
            fee.setBillMonth(billMonth);
            fee.setManagementFee(managementFee.setScale(2, RoundingMode.HALF_UP));
            fee.setPublicFee(publicFee.setScale(2, RoundingMode.HALF_UP));
            fee.setTotalFee(totalFee.setScale(2, RoundingMode.HALF_UP));
            fee.setPayStatus("UNPAID");

            createdFees.add(feeRepository.save(fee));
        }

        return createdFees;
    }

    public List<PropertyFee> listByOwner(Long ownerId, Long roomId) {
        Set<PropertyFee> fees = new HashSet<>();
        
        if (roomId != null) {
            fees.addAll(listByRoom(roomId));
        }
        
        List<HousePurchaseApplication> houseApps = housePurchaseRepository.findByOwnerId(ownerId);
        for (HousePurchaseApplication app : houseApps) {
            if (app.getId() != null) {
                fees.addAll(feeRepository.findByHousePurchaseId(app.getId()));
            }
        }
        
        List<ParkingApplication> parkingApps = parkingApplicationRepository.findByOwnerId(ownerId);
        for (ParkingApplication app : parkingApps) {
            if (app.getId() != null) {
                fees.addAll(feeRepository.findByParkingPurchaseId(app.getId()));
            }
        }
        
        return new ArrayList<>(fees);
    }

    // ✅ 获取逾期账单
    public List<PropertyFee> getOverdueFees(int daysOverdue) {
        LocalDate today = LocalDate.now();

        return feeRepository.findAll().stream()
                .filter(f -> "UNPAID".equals(f.getPayStatus()))
                .filter(f -> {
                    try {
                        YearMonth billMonth = YearMonth.parse(f.getBillMonth());
                        LocalDate dueDate = billMonth.atEndOfMonth().plusDays(15);
                        return today.isAfter(dueDate.plusDays(daysOverdue));
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    // ✅ 标记逾期
    @Transactional
    public void markAsOverdue() {
        LocalDate today = LocalDate.now();

        feeRepository.findAll().stream()
                .filter(f -> "UNPAID".equals(f.getPayStatus()))
                .forEach(f -> {
                    try {
                        YearMonth billMonth = YearMonth.parse(f.getBillMonth());
                        LocalDate dueDate = billMonth.atEndOfMonth().plusDays(15);
                        if (today.isAfter(dueDate)) {
                            f.setPayStatus("OVERDUE");
                            feeRepository.save(f);
                        }
                    } catch (Exception ignored) {
                    }
                });
    }

    // ✅ 生成购房款账单
    @Transactional
    public PropertyFee generateHousePurchaseFee(Long roomId, String unitNo, Double amount, Long applicationId) {
        PropertyFee fee = new PropertyFee();
        fee.setRoomId(roomId);
        fee.setBillMonth(YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        fee.setManagementFee(BigDecimal.valueOf(amount));
        fee.setPublicFee(BigDecimal.ZERO);
        fee.setTotalFee(BigDecimal.valueOf(amount));
        fee.setPayStatus("UNPAID");
        fee.setFeeType("HOUSE_PURCHASE");
        fee.setHousePurchaseId(applicationId);
        return feeRepository.save(fee);
    }

    // ✅ 生成车位购买款账单
    @Transactional
    public PropertyFee generateParkingPurchaseFee(Long ownerId, String spaceNo, Double amount, Long applicationId) {
        PropertyFee fee = new PropertyFee();
        
        // 根据 ownerId 查询业主的 roomId
        Long roomId = ownerRepository.findById(ownerId)
                .map(Owner::getRoomId)
                .orElse(ownerId);
        
        fee.setRoomId(roomId);
        fee.setBillMonth(YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        fee.setManagementFee(BigDecimal.valueOf(amount));
        fee.setPublicFee(BigDecimal.ZERO);
        fee.setTotalFee(BigDecimal.valueOf(amount));
        fee.setPayStatus("UNPAID");
        fee.setFeeType("PARKING_PURCHASE");
        fee.setParkingPurchaseId(applicationId);
        return feeRepository.save(fee);
    }
}