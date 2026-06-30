package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.entity.Owner;
import com.autogen.propmgmt.entity.User;
import com.autogen.propmgmt.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ValidationHelper {

    private static final Pattern PHONE = Pattern.compile("^1\\d{10}$");
    private static final Pattern EMAIL = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern OWNER_NO = Pattern.compile("^[A-Za-z0-9]{4,20}$");

    private final OwnerRepository ownerRepository;

    public void validatePhone(String phone, boolean required) {
        if (!StringUtils.hasText(phone)) {
            if (required) throw new BusinessException("手机号不能为空");
            return;
        }
        if (phone.length() != 11 || !PHONE.matcher(phone).matches()) {
            throw new BusinessException("手机号必须为11位且以1开头");
        }
    }

    public void validateEmail(String email) {
        if (StringUtils.hasText(email) && !EMAIL.matcher(email).matches()) {
            throw new BusinessException("邮箱格式不正确");
        }
    }

    public void validateOwner(Owner owner, boolean isCreate) {
        if (!StringUtils.hasText(owner.getName())) {
            throw new BusinessException("姓名不能为空");
        }
        if (!StringUtils.hasText(owner.getOwnerNo())) {
            throw new BusinessException("业主编号不能为空");
        }
        if (!OWNER_NO.matcher(owner.getOwnerNo()).matches()) {
            throw new BusinessException("业主编号仅支持4-20位字母或数字");
        }
        if (isCreate && ownerRepository.findByOwnerNo(owner.getOwnerNo()).isPresent()) {
            throw new BusinessException("业主编号已存在");
        }
        validatePhone(owner.getPhone(), false);
        validateEmail(owner.getEmail());
    }

    public void validateUserProfile(User user) {
        if (!StringUtils.hasText(user.getName())) {
            throw new BusinessException("姓名不能为空");
        }
        validatePhone(user.getPhone(), false);
        validateEmail(user.getEmail());
    }
}
