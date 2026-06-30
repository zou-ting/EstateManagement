package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.entity.HousePurchaseApplication;
import com.autogen.propmgmt.entity.PropUnit;
import com.autogen.propmgmt.entity.PropertyFee;
import com.autogen.propmgmt.repository.HousePurchaseApplicationRepository;
import com.autogen.propmgmt.repository.PropUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HousePurchaseApplicationService {

    private final HousePurchaseApplicationRepository applicationRepository;
    private final PropUnitRepository unitRepository;
    private final PropertyFeeService propertyFeeService;

    @Transactional
    public HousePurchaseApplication submit(HousePurchaseApplication application) {
        PropUnit unit = unitRepository.findById(application.getUnitId())
                .orElseThrow(() -> new BusinessException("房屋不存在"));
        if (unit.getStatus() != 2) {
            throw new BusinessException("该房屋不可购买");
        }

        List<HousePurchaseApplication> existingApps = applicationRepository.findByUnitId(application.getUnitId())
                .stream().filter(a -> !"CANCELLED".equals(a.getStatus()) && !"REJECTED".equals(a.getStatus())).toList();
        if (!existingApps.isEmpty()) {
            throw new BusinessException("该房屋已有未处理的购买申请");
        }

        application.setStatus("PENDING");
        return applicationRepository.save(application);
    }

    public void cancel(Long id) {
        HousePurchaseApplication app = getById(id);
        if (!"PENDING".equals(app.getStatus()) && !"APPROVED".equals(app.getStatus())) {
            throw new BusinessException("只有待审批或待付款的申请可以撤销");
        }
        app.setStatus("CANCELLED");
        applicationRepository.save(app);
    }

    public List<HousePurchaseApplication> listByOwner(Long ownerId) {
        return applicationRepository.findByOwnerId(ownerId);
    }

    public List<HousePurchaseApplication> getPendingList() {
        return applicationRepository.findByStatus("PENDING");
    }

    public List<HousePurchaseApplication> listAll() {
        return applicationRepository.findAll();
    }

    @Transactional
    public HousePurchaseApplication approve(Long id, boolean approved, String remark, Double purchaseAmount) {
        HousePurchaseApplication app = getById(id);
        if (!"PENDING".equals(app.getStatus())) {
            throw new BusinessException("该申请已处理");
        }

        app.setApprovalDate(LocalDateTime.now());
        app.setApprovalRemark(remark);

        if (approved) {
            if (purchaseAmount == null || purchaseAmount <= 0) {
                throw new BusinessException("请输入正确的购房金额");
            }
            app.setStatus("APPROVED");
            app.setPurchaseAmount(purchaseAmount);

            // 生成购房款账单
            propertyFeeService.generateHousePurchaseFee(
                    app.getUnitId(),
                    app.getUnitNo(),
                    purchaseAmount,
                    app.getId()
            );
        } else {
            app.setStatus("REJECTED");
        }

        return applicationRepository.save(app);
    }

    @Transactional
    public HousePurchaseApplication markAsPaid(Long id) {
        HousePurchaseApplication app = getById(id);
        if (!"APPROVED".equals(app.getStatus())) {
            throw new BusinessException("只有待付款的申请可以标记为已付款");
        }
        app.setStatus("PAID");
        return applicationRepository.save(app);
    }

    @Transactional
    public HousePurchaseApplication complete(Long id) {
        HousePurchaseApplication app = getById(id);
        if (!"PAID".equals(app.getStatus())) {
            throw new BusinessException("只有已付款的申请可以完成");
        }
        app.setStatus("COMPLETED");

        // 将房屋关联到业主
        PropUnit unit = unitRepository.findById(app.getUnitId())
                .orElseThrow(() -> new BusinessException("房屋不存在"));
        unit.setStatus(1);  // 已入住
        unit.setOwnerCount(unit.getOwnerCount() != null ? unit.getOwnerCount() + 1 : 1);
        unit.setOwnerId(app.getOwnerId());
        unitRepository.save(unit);

        return applicationRepository.save(app);
    }

    public HousePurchaseApplication getById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请记录不存在"));
    }
}
