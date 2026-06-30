package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.dto.LoginRequest;
import com.autogen.propmgmt.dto.LoginResponse;
import com.autogen.propmgmt.entity.Owner;
import com.autogen.propmgmt.entity.User;
import com.autogen.propmgmt.repository.OwnerRepository;
import com.autogen.propmgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Set<String> ADMIN_PORTAL_ROLES = Set.of("ADMIN", "PROPERTY_MANAGER");
    private static final Set<String> CLIENT_PORTAL_ROLES = Set.of("OWNER");

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;

    public LoginResponse login(LoginRequest request, String portal) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!user.getPassword().equals(request.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已禁用");
        }
        validatePortal(user.getRole(), portal);
        Long ownerId = ownerRepository.findByUserId(user.getId())
                .map(Owner::getId)
                .orElse(null);
        String token = UUID.randomUUID().toString().replace("-", "");
        String avatar = resolveAvatar(user);
        return new LoginResponse(user.getId(), user.getUsername(), user.getName(),
                user.getRole(), token, ownerId, avatar);
    }

    private String resolveAvatar(User user) {
        if (StringUtils.hasText(user.getAvatar())) {
            return user.getAvatar();
        }
        return ownerRepository.findByUserId(user.getId())
                .map(Owner::getAvatar)
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    private void validatePortal(String role, String portal) {
        if ("admin".equals(portal) && !ADMIN_PORTAL_ROLES.contains(role)) {
            throw new BusinessException("请使用客户端登录");
        }
        if ("client".equals(portal) && !CLIENT_PORTAL_ROLES.contains(role)) {
            throw new BusinessException("请使用管理端登录");
        }
    }
}
