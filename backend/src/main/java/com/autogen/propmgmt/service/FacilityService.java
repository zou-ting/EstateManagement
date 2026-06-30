package com.autogen.propmgmt.service;

import com.autogen.propmgmt.common.BusinessException;
import com.autogen.propmgmt.entity.Facility;
import com.autogen.propmgmt.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository repository;

    public List<Facility> list() {
        return repository.findAll();
    }

    public Facility getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("设备不存在"));
    }

    public Facility save(Facility entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}