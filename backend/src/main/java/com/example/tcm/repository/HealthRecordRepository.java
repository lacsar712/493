package com.example.tcm.repository;

import com.example.tcm.entity.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByPatientId(Long patientId);
    List<HealthRecord> findByDoctorId(Long doctorId);
    List<HealthRecord> findByPatientIdOrderByVisitDateDesc(Long patientId);
    Optional<HealthRecord> findTopByPatientIdOrderByVisitDateDesc(Long patientId);
    // Or order by createTime if visitDate is not reliable for "latest" entry time
    Optional<HealthRecord> findTopByPatientIdOrderByCreateTimeDesc(Long patientId);
}
