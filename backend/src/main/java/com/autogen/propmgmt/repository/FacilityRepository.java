package com.autogen.propmgmt.repository;

import com.autogen.propmgmt.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
    List<Facility> findAll();
}