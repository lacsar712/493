package com.example.tcm.repository;

import com.example.tcm.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByMainDoctorId(Long mainDoctorId);
    
    Page<Patient> findByNameContaining(String name, Pageable pageable);
    Page<Patient> findByMainDoctorIdAndNameContaining(Long mainDoctorId, String name, Pageable pageable);
    Page<Patient> findByMainDoctorId(Long mainDoctorId, Pageable pageable);
    
    List<Patient> findByName(String name);
}
