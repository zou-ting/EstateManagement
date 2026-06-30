package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.entity.Owner;
import com.autogen.propmgmt.entity.User;
import com.autogen.propmgmt.repository.OwnerRepository;
import com.autogen.propmgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import static com.autogen.propmgmt.config.PinyinUtil.toPinyin;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;

    public List<Owner> list(String keyword) {
        if (StringUtils.hasText(keyword)) {
            return ownerRepository.findByNameContaining(keyword);
        }
        return ownerRepository.findAll();
    }

    public Owner getById(Long id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("业主不存在"));
    }

    public Owner getByUserId(Long userId) {
        return ownerRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("未找到住宿信息"));
    }

    @Transactional
    public Owner save(Owner owner) {
        if (owner.getId() == null) {
            // =============================================
            // 1. 生成邮箱（拼音 + @property.com）
            // =============================================
            String pinyin = toPinyin(owner.getName());
            String email = pinyin + "@property.com";
            owner.setEmail(email);

            // =============================================
            // 2. 保存 owner
            // =============================================
            if (owner.getRegisterDate() == null) {
                owner.setRegisterDate(LocalDate.now());
            }
            Owner savedOwner = ownerRepository.save(owner);

            // =============================================
            // 3. 生成用户名（owner + ID后三位）
            // =============================================
            String username = "owner" + String.format("%03d", savedOwner.getId());

            // 检查用户名冲突
            int suffix = 1;
            String baseUsername = username;
            while (userRepository.findByUsername(username).isPresent()) {
                username = baseUsername + suffix;
                suffix++;
            }

            // =============================================
            // 4. 创建系统用户
            // =============================================
            User user = new User();
            user.setUsername(username);
            user.setPassword("123456");
            user.setRole("OWNER");
            user.setName(owner.getName());
            user.setPhone(owner.getPhone());
            user.setEmail(email);
            user.setStatus(owner.getStatus() != null ? owner.getStatus() : 1);
            user = userRepository.save(user);

            // =============================================
            // 5. 关联 user_id
            // =============================================
            savedOwner.setUserId(user.getId());
            return ownerRepository.save(savedOwner);
        }
        return update(owner.getId(), owner);
    }
    private String generateUsername(Owner owner) {
        String base = owner.getName().replaceAll("[\\u4e00-\\u9fa5]", "");
        if (!StringUtils.hasText(base)) {
            base = owner.getName().substring(0, Math.min(owner.getName().length(), 2));
        }
        String username = base.toLowerCase() + owner.getOwnerNo();
        if (username.length() > 50) {
            username = username.substring(0, 50);
        }
        return username;
    }

    @Transactional
    public Owner update(Long id, Owner updateData) {
        Owner owner = getById(id);

        if (StringUtils.hasText(updateData.getName())) {
            owner.setName(updateData.getName());
        }
        if (StringUtils.hasText(updateData.getGender())) {
            owner.setGender(updateData.getGender());
        }
        if (StringUtils.hasText(updateData.getPhone())) {
            owner.setPhone(updateData.getPhone());
        }
        if (StringUtils.hasText(updateData.getEmail())) {
            owner.setEmail(updateData.getEmail());
        }
        if (StringUtils.hasText(updateData.getPropertyCert())) {
            owner.setPropertyCert(updateData.getPropertyCert());
        }
        if (StringUtils.hasText(updateData.getUnitSection())) {
            owner.setUnitSection(updateData.getUnitSection());
        }
        if (updateData.getStatus() != null) {
            owner.setStatus(updateData.getStatus());
            // 同步更新用户状态
            if (owner.getUserId() != null) {
                userRepository.findById(owner.getUserId()).ifPresent(user -> {
                    // 业主在册(1) -> 用户启用(1)，业主已迁出(0) -> 用户禁用(0)
                    user.setStatus(updateData.getStatus());
                    userRepository.save(user);
                });
            }
        }
        if (updateData.getRoomId() != null) {
            owner.setRoomId(updateData.getRoomId());
        }
        if (updateData.getBuildingId() != null) {
            owner.setBuildingId(updateData.getBuildingId());
        }
        if (StringUtils.hasText(updateData.getAvatar())) {
            owner.setAvatar(updateData.getAvatar());
        }
        if (updateData.getRegisterDate() != null) {
            owner.setRegisterDate(updateData.getRegisterDate());
        }

        Owner savedOwner = ownerRepository.save(owner);

        // 同步更新用户表
        if (savedOwner.getUserId() != null) {
            userRepository.findById(savedOwner.getUserId()).ifPresent(user -> {
                if (StringUtils.hasText(updateData.getName())) {
                    user.setName(updateData.getName());
                }
                if (StringUtils.hasText(updateData.getPhone())) {
                    user.setPhone(updateData.getPhone());
                }
                if (StringUtils.hasText(updateData.getEmail())) {
                    user.setEmail(updateData.getEmail());
                }
                if (updateData.getStatus() != null) {
                    user.setStatus(updateData.getStatus());
                }
                if (StringUtils.hasText(updateData.getAvatar())) {
                    user.setAvatar(updateData.getAvatar());
                }
                userRepository.save(user);
            });
        }

        return savedOwner;
    }

    @Transactional
    public void delete(Long id) {
        Owner owner = getById(id);
        
        // 同步删除用户表记录
        if (owner.getUserId() != null) {
            userRepository.deleteById(owner.getUserId());
        }
        
        ownerRepository.deleteById(id);
    }
}