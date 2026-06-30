package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.dto.RegisterRequest;
import com.autogen.propmgmt.entity.Owner;
import com.autogen.propmgmt.entity.User;
import com.autogen.propmgmt.repository.OwnerRepository;
import com.autogen.propmgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final ValidationHelper validationHelper;

    @Transactional
    public Owner registerOwner(RegisterRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }
        if (ownerRepository.findByOwnerNo(req.getOwnerNo()).isPresent()) {
            throw new BusinessException("业主编号已存在");
        }
        validationHelper.validatePhone(req.getPhone(), true);
        validationHelper.validateEmail(req.getEmail());

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setRole("OWNER");
        user.setName(req.getName());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setStatus(1);
        user = userRepository.save(user);

        Owner owner = new Owner();
        owner.setOwnerNo(req.getOwnerNo());
        owner.setName(req.getName());
        owner.setGender(req.getGender());
        owner.setPhone(req.getPhone());
        owner.setEmail(req.getEmail());
        owner.setUserId(user.getId());
        owner.setRegisterDate(LocalDate.now());
        owner.setStatus(1);
        return ownerRepository.save(owner);
    }
}
