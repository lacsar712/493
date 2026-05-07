package com.example.tcm.controller;

import com.example.tcm.common.Result;
import com.example.tcm.entity.HealthRecord;
import com.example.tcm.entity.Patient;
import com.example.tcm.entity.TcmResult;
import com.example.tcm.entity.User;
import com.example.tcm.repository.HealthRecordRepository;
import com.example.tcm.repository.PatientRepository;
import com.example.tcm.repository.TcmResultRepository;
import com.example.tcm.repository.UserRepository;
import com.example.tcm.security.JwtUtil;
import com.example.tcm.service.SuggestionService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/records")
public class HealthRecordController {

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TcmResultRepository tcmResultRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SuggestionService suggestionService;

    // Creation moved to /api/doctor/records

    @GetMapping
    public Result<List<HealthRecord>> listRecords(@RequestParam(required = false) Long patientId, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        if ("ADMIN".equals(role)) {
             if (patientId != null) {
                 return Result.success(healthRecordRepository.findByPatientId(patientId));
             }
             return Result.success(healthRecordRepository.findAll());
        } else if ("PATIENT".equals(role)) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getPatientId() == null) {
                return Result.error(400, "用户未关联患者信息");
            }
            return Result.success(healthRecordRepository.findByPatientIdOrderByVisitDateDesc(user.getPatientId()));
         } else if ("DOCTOR".equals(role)) {
            if (patientId != null) {
                // Check if this patient belongs to doctor
                Patient patient = patientRepository.findById(patientId).orElse(null);
                if (patient != null && Objects.equals(patient.getMainDoctorId(), userId)) {
                    return Result.success(healthRecordRepository.findByPatientId(patientId));
                }
                return Result.error(403, "拒绝访问：不是您的患者");
            } else {
                 // Doctor wants to list all records? Usually they browse via Patient -> Records.
                 // But if they request all, we should probably filter by their patients or return empty.
                 // Let's just return empty or error for now if no patientId provided, to be safe.
                 // Or better: find all records where doctor_id = userId (records they created).
                 // But repository doesn't have findByDoctorId yet. Let's skip implementing new repo method and just require patientId for Doctor query for now.
                 return Result.error(400, "医生必须指定患者ID");
            }
        }
        
        return Result.error(403, "拒绝访问");
    }
    
    @PutMapping("/{id}")
    public Result<HealthRecord> updateRecord(@PathVariable Long id, @Validated(HealthRecord.Update.class) @RequestBody HealthRecord recordDetails, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        HealthRecord record = healthRecordRepository.findById(id).orElse(null);
        if (record == null) {
            return Result.error(404, "未找到记录");
        }

        // Admin can update any record, Doctor can only update records they created
        if ("DOCTOR".equals(role)) {
             // Strict check: Doctor must be the creator of the record
             if (!Objects.equals(record.getDoctorId(), userId)) {
                 return Result.error(403, "拒绝访问：只能编辑自己创建的记录");
             }
        } else if (!"ADMIN".equals(role)) {
            return Result.error(403, "拒绝访问");
        }
        
        record.setComplaint(recordDetails.getComplaint());
        record.setDiagnosis(recordDetails.getDiagnosis());
        record.setSuggestion(recordDetails.getSuggestion());
        record.setVisitDate(recordDetails.getVisitDate());
        // Body type is usually derived, but let's allow manual edit if needed
        if (recordDetails.getBodyType() != null) {
            record.setBodyType(recordDetails.getBodyType());
        }

        return Result.success(healthRecordRepository.save(record));
    }

    @GetMapping("/{id}")
    public Result<HealthRecord> getRecord(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        HealthRecord record = healthRecordRepository.findById(id).orElse(null);
        if (record == null) {
            return Result.error(404, "未找到记录");
        }

        if ("DOCTOR".equals(role)) {
             Patient patient = patientRepository.findById(record.getPatientId()).orElse(null);
             if (patient == null || !Objects.equals(patient.getMainDoctorId(), userId)) {
                 return Result.error(403, "拒绝访问：不是您的患者");
             }
        } else if ("PATIENT".equals(role)) {
            User user = userRepository.findById(userId).orElse(null);
            // Patient can only see their own
            if (user == null || !Objects.equals(user.getPatientId(), record.getPatientId())) {
                 return Result.error(403, "拒绝访问");
            }
        } else if (!"ADMIN".equals(role)) {
            return Result.error(403, "拒绝访问");
        }
        
        return Result.success(record);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);

        if (!"ADMIN".equals(role)) {
            return Result.error(403, "拒绝访问：只有管理员可以删除记录");
        }

        if (!healthRecordRepository.existsById(id)) {
            return Result.error(404, "未找到记录");
        }

        healthRecordRepository.deleteById(id);
        return Result.success(null);
    }
}
