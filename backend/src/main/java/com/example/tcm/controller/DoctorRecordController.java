package com.example.tcm.controller;

import com.example.tcm.common.Result;
import com.example.tcm.entity.HealthRecord;
import com.example.tcm.entity.Patient;
import com.example.tcm.entity.TcmResult;
import com.example.tcm.repository.HealthRecordRepository;
import com.example.tcm.repository.PatientRepository;
import com.example.tcm.repository.TcmResultRepository;
import com.example.tcm.security.JwtUtil;
import com.example.tcm.service.SuggestionService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/doctor/records")
public class DoctorRecordController {

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TcmResultRepository tcmResultRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SuggestionService suggestionService;

    @GetMapping
    public Result<?> list(@RequestParam Long patientId, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        if (!"DOCTOR".equals(role)) {
            return Result.error(403, "只有医生可以查看此列表");
        }

        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null) {
            return Result.error(404, "未找到患者");
        }
        if (!Objects.equals(patient.getMainDoctorId(), userId)) {
            return Result.error(403, "该患者未分配给您");
        }

        return Result.success(healthRecordRepository.findByPatientIdOrderByVisitDateDesc(patientId));
    }

    @PostMapping
    public Result<HealthRecord> create(@Validated(HealthRecord.Create.class) @RequestBody HealthRecord record, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        if (!"DOCTOR".equals(role)) {
            return Result.error(403, "只有医生可以创建健康记录");
        }

        Patient patient = patientRepository.findById(record.getPatientId()).orElse(null);
        if (patient == null) {
            return Result.error(404, "未找到患者");
        }
        if (!Objects.equals(patient.getMainDoctorId(), userId)) {
            return Result.error(403, "该患者未分配给您");
        }

        record.setDoctorId(userId);

        if (record.getBodyType() == null || record.getBodyType().trim().isEmpty()) {
            Optional<TcmResult> lastResult = tcmResultRepository.findTopByPatientIdOrderByCreatedTimeDesc(record.getPatientId());
            if (lastResult.isPresent()) {
                record.setBodyType(lastResult.get().getBodyType());
            }
        }

        if (record.getSuggestion() == null || record.getSuggestion().trim().isEmpty()) {
            String bodyType = record.getBodyType();
            if (bodyType != null && !bodyType.isEmpty()) {
                record.setSuggestion(suggestionService.getSuggestion(bodyType));
            } else {
                record.setSuggestion(suggestionService.getSuggestion("Unknown"));
            }
        }

        if (record.getVisitDate() == null) {
            record.setVisitDate(LocalDate.now());
        }

        return Result.success(healthRecordRepository.save(record));
    }
}
