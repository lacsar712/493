package com.example.tcm.repository;

import com.example.tcm.entity.TcmResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TcmResultRepository extends JpaRepository<TcmResult, Long> {
    List<TcmResult> findByPatientId(Long patientId);
    Optional<TcmResult> findTopByPatientIdOrderByCreatedTimeDesc(Long patientId);
}
