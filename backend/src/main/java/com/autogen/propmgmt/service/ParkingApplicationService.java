package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.controller.ParkingApplicationController;
import com.autogen.propmgmt.entity.ParkingApplication;
import com.autogen.propmgmt.entity.ParkingSpace;
import com.autogen.propmgmt.repository.ParkingApplicationRepository;
import com.autogen.propmgmt.repository.ParkingSpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingApplicationService {

    private final ParkingApplicationRepository applicationRepository;
    private final ParkingSpaceRepository parkingSpaceRepository;
    private final PropertyFeeService propertyFeeService;

    // 获取空车位（业主端选择）
    public List<ParkingSpace> getAvailableSpaces() {
        return parkingSpaceRepository.findByStatus(1); // 1=空闲
    }

    // 业主提交申请
    @Transactional
    public ParkingApplication submit(ParkingApplication application) {
        // 校验车位是否存在且空闲
        ParkingSpace space = parkingSpaceRepository.findById(application.getParkingSpaceId())
                .orElseThrow(() -> new BusinessException("车位不存在"));
        if (space.getStatus() != 1) {
            throw new BusinessException("该车位已被占用或停用");
        }

        // 校验申请类型是否有效
        String type = application.getApplicationType();
        if (!List.of("RENT", "PURCHASE", "ADD", "CHANGE").contains(type)) {
            throw new BusinessException("无效的申请类型");
        }

        application.setStatus("PENDING");
        return applicationRepository.save(application);
    }

    // 取消申请（仅待审批）
    public void cancel(Long id) {
        ParkingApplication app = getById(id);
        if (!"PENDING".equals(app.getStatus())) {
            throw new BusinessException("只有待审批的申请可以取消");
        }
        app.setStatus("CANCELLED");
        applicationRepository.save(app);
    }

    // 业主查询自己的申请记录
    public List<ParkingApplication> listByOwner(Long ownerId) {
        return applicationRepository.findByOwnerId(ownerId);
    }

    // 管理员获取待审批列表
    public List<ParkingApplication> getPendingList() {
        return applicationRepository.findByStatus("PENDING");
    }

    // 管理员审批
    // 在 approve 方法中，审批通过时自动处理旧车位
    @Transactional
    public ParkingApplication approve(Long id, boolean approved, String remark, Double purchaseAmount) {
        ParkingApplication app = getById(id);
        if (!"PENDING".equals(app.getStatus())) {
            throw new BusinessException("该申请已处理");
        }

        app.setApprovalDate(LocalDateTime.now());
        app.setApprovalRemark(remark);

        if (approved) {
            // 获取车位信息
            ParkingSpace space = parkingSpaceRepository.findById(app.getParkingSpaceId())
                    .orElseThrow(() -> new BusinessException("车位不存在"));
            
            // 判断是否为购买类型（申请类型为PURCHASE，或申请类型为ADD但车位类型为FIXED）
            boolean isPurchase = "PURCHASE".equals(app.getApplicationType()) || 
                                ("ADD".equals(app.getApplicationType()) && "FIXED".equals(space.getType()));
            
            if (isPurchase) {
                // 购买类型需要输入金额
                if (purchaseAmount == null || purchaseAmount <= 0) {
                    throw new BusinessException("购买类型申请请输入正确的购买金额");
                }
                app.setStatus("APPROVED");
                app.setPurchaseAmount(BigDecimal.valueOf(purchaseAmount));
                
                // 生成车位购买账单
                propertyFeeService.generateParkingPurchaseFee(
                        app.getOwnerId(),
                        space.getSpaceNo(),
                        purchaseAmount,
                        app.getId()
                );
            } else {
                app.setStatus("APPROVED");
                
                // 计算租赁费用
                double monthlyFee = space.getMonthlyFee() != null ? space.getMonthlyFee().doubleValue() : 0;
                int months = getMonths(app.getRentalDuration());
                double totalAmount = monthlyFee * months;
                app.setPurchaseAmount(BigDecimal.valueOf(totalAmount));
                
                // 生成车位租赁账单
                propertyFeeService.generateParkingPurchaseFee(
                        app.getOwnerId(),
                        space.getSpaceNo(),
                        totalAmount,
                        app.getId()
                );
            }
        } else {
            app.setStatus("REJECTED");
        }

        return applicationRepository.save(app);
    }

    private int getMonths(String rentalDuration) {
        if (rentalDuration == null) return 1;
        return switch (rentalDuration) {
            case "MONTHLY" -> 1;
            case "QUARTERLY" -> 3;
            case "YEARLY" -> 12;
            case "DAILY" -> 0;
            case "HOURLY" -> 0;
            default -> 1;
        };
    }

    // 标记为已付款（购买/租赁类型）
    @Transactional
    public ParkingApplication markAsPaid(Long id) {
        ParkingApplication app = getById(id);
        if (!"APPROVED".equals(app.getStatus())) {
            throw new BusinessException("只有待付款的申请可以标记为已付款");
        }
        
        ParkingSpace space = parkingSpaceRepository.findById(app.getParkingSpaceId())
                .orElseThrow(() -> new BusinessException("车位不存在"));
        
        app.setStatus("DONE");
        applicationRepository.save(app);
        
        // 付款成功后直接分配车位给业主
        space.setStatus(2);
        space.setOwnerId(app.getOwnerId());
        space.setPlateNumber(app.getPlateNumber());
        space.setVehicleType(app.getVehicleType());
        
        // 判断是否为购买类型
        boolean isPurchase = "PURCHASE".equals(app.getApplicationType()) || 
                            ("ADD".equals(app.getApplicationType()) && "FIXED".equals(space.getType()));
        
        if (isPurchase) {
            space.setRentalDuration("PURCHASE");
            space.setRentStartDate(null);
            space.setRentEndDate(null);
        } else {
            space.setRentalDuration(app.getRentalDuration());
            space.setRentStartDate(app.getStartDate());
            space.setRentEndDate(app.getEndDate());
        }
        
        parkingSpaceRepository.save(space);
        
        return app;
    }

    // 完成购买（管理员确认后）
    @Transactional
    public ParkingApplication complete(Long id) {
        ParkingApplication app = getById(id);
        if (!"PAID".equals(app.getStatus())) {
            throw new BusinessException("只有已付款的申请可以完成");
        }
        
        ParkingSpace space = parkingSpaceRepository.findById(app.getParkingSpaceId())
                .orElseThrow(() -> new BusinessException("车位不存在"));
        
        boolean isPurchase = "PURCHASE".equals(app.getApplicationType()) || 
                            ("ADD".equals(app.getApplicationType()) && "FIXED".equals(space.getType()));
        
        if (!isPurchase) {
            throw new BusinessException("只有购买类型的申请可以完成");
        }
        
        app.setStatus("DONE");

        // 分配车位给业主
        space.setStatus(2);
        space.setOwnerId(app.getOwnerId());
        space.setPlateNumber(app.getPlateNumber());
        space.setVehicleType(app.getVehicleType());
        space.setRentalDuration("PURCHASE");
        space.setRentStartDate(null);
        space.setRentEndDate(null);
        parkingSpaceRepository.save(space);

        return applicationRepository.save(app);
    }

    // 续租申请
    @Transactional
    public ParkingApplication renew(ParkingApplicationController.RenewRequest request) {
        ParkingSpace space = parkingSpaceRepository.findById(request.getParkingSpaceId())
                .orElseThrow(() -> new BusinessException("车位不存在"));

        if (space.getOwnerId() == null || !space.getOwnerId().equals(request.getOwnerId())) {
            throw new BusinessException("您不是该车位的使用人");
        }

        ParkingApplication app = new ParkingApplication();
        app.setParkingSpaceId(request.getParkingSpaceId());
        app.setOwnerId(request.getOwnerId());
        app.setApplicationType("RENEW");
        app.setRentalDuration(request.getRentalDuration());
        app.setReason(request.getReason());
        app.setStartDate(space.getRentEndDate() != null ? space.getRentEndDate().plusDays(1) : LocalDate.now());
        // 根据期限计算结束日期
        app.setEndDate(calculateEndDate(app.getStartDate(), request.getRentalDuration()));
        app.setStatus("PENDING");

        return applicationRepository.save(app);
    }

    // 退租申请
    @Transactional
    public ParkingApplication cancelRent(ParkingApplicationController.CancelRentRequest request) {
        ParkingSpace space = parkingSpaceRepository.findById(request.getParkingSpaceId())
                .orElseThrow(() -> new BusinessException("车位不存在"));

        if (space.getOwnerId() == null || !space.getOwnerId().equals(request.getOwnerId())) {
            throw new BusinessException("您不是该车位的使用人");
        }

        ParkingApplication app = new ParkingApplication();
        app.setParkingSpaceId(request.getParkingSpaceId());
        app.setOwnerId(request.getOwnerId());
        app.setApplicationType("CANCEL");
        app.setReason(request.getReason());
        app.setStatus("PENDING");

        return applicationRepository.save(app);
    }

    // 获取详情
    public ParkingApplication getById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请记录不存在"));
    }

    // 计算结束日期
    private LocalDate calculateEndDate(LocalDate start, String duration) {
        return switch (duration) {
            case "MONTHLY" -> start.plusMonths(1);
            case "QUARTERLY" -> start.plusMonths(3);
            case "YEARLY" -> start.plusYears(1);
            default -> start.plusMonths(1);
        };
    }
}