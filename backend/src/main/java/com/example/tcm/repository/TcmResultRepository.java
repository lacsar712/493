package com.example.tcm.repository;

import com.example.tcm.entity.TcmResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TcmResultRepository extends JpaRepository<TcmResult, Long> {
    List<TcmResult> findByPatientId(Long patientId);
    Optional<TcmResult> findTopByPatientIdOrderByCreatedTimeDesc(Long patientId);
    Page<TcmResult> findByPatientId(Long patientId, Pageable pageable);
    Page<TcmResult> findByPatientIdIn(List<Long> patientIds, Pageable pageable);
}
