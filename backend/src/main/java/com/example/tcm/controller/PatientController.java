package com.example.tcm.controller;

import com.example.tcm.common.Result;
import com.example.tcm.dto.PatientProfileDto;
import com.example.tcm.entity.HealthRecord;
import com.example.tcm.entity.Patient;
import com.example.tcm.entity.TcmResult;
import com.example.tcm.entity.User;
import com.example.tcm.repository.HealthRecordRepository;
import com.example.tcm.repository.PatientRepository;
import com.example.tcm.repository.TcmResultRepository;
import com.example.tcm.repository.UserRepository;
import com.example.tcm.security.JwtUtil;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api")
public class PatientController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TcmResultRepository tcmResultRepository;

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // --- Patient Management (Admin & Doctor) ---

    @GetMapping("/patients")
    public Result<List<Patient>> listPatients(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        List<Patient> patients;
        if ("ADMIN".equals(role)) {
            patients = patientRepository.findAll();
        } else if ("DOCTOR".equals(role)) {
            // Doctor sees only their patients
            patients = patientRepository.findByMainDoctorId(userId);
        } else {
            return Result.error(403, "拒绝访问");
        }
        
        populateDoctorNames(patients);
        return Result.success(patients);
    }

    @GetMapping("/patients/page")
    public Result<Page<Patient>> pagePatients(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            HttpServletRequest request) {
        
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);
        
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<Patient> patientPage;
        if ("ADMIN".equals(role)) {
            if (name != null && !name.trim().isEmpty()) {
                patientPage = patientRepository.findByNameContaining(name, pageRequest);
            } else {
                patientPage = patientRepository.findAll(pageRequest);
            }
        } else if ("DOCTOR".equals(role)) {
            if (name != null && !name.trim().isEmpty()) {
                patientPage = patientRepository.findByMainDoctorIdAndNameContaining(userId, name, pageRequest);
            } else {
                patientPage = patientRepository.findByMainDoctorId(userId, pageRequest);
            }
        } else {
            return Result.error(403, "拒绝访问");
        }
        
        populateDoctorNames(patientPage.getContent());
        return Result.success(patientPage);
    }

    private void populateDoctorNames(List<Patient> patients) {
        if (patients == null || patients.isEmpty()) return;
        
        java.util.Set<Long> doctorIds = patients.stream()
            .map(Patient::getMainDoctorId)
            .filter(Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());
        
        if (doctorIds.isEmpty()) return;
        
        List<User> doctors = userRepository.findAllById(doctorIds);
        java.util.Map<Long, String> doctorMap = doctors.stream()
            .collect(java.util.stream.Collectors.toMap(User::getId, User::getUsername));
            
        for (Patient p : patients) {
            if (p.getMainDoctorId() != null) {
                p.setDoctorName(doctorMap.get(p.getMainDoctorId()));
            }
        }
    }

    @PostMapping("/patients")
    public Result<Patient> createPatient(@Valid @RequestBody Patient patient, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        // Manual validation
        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            return Result.error(400, "姓名不能为空");
        }

        if ("DOCTOR".equals(role)) {
            patient.setMainDoctorId(userId);
        } else if (!"ADMIN".equals(role)) {
            return Result.error(403, "拒绝访问");
        }
        // Admin can set mainDoctorId in request body, Doctor's is overwritten/set to self
        
        return Result.success(patientRepository.save(patient));
    }

    @PutMapping("/patients/{id}")
    public Result<Patient> updatePatient(@PathVariable Long id, @Valid @RequestBody Patient patientDetails, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        Patient patient = patientRepository.findById(id).orElse(null);
        if (patient == null) {
            return Result.error(404, "未找到患者");
        }

        if ("DOCTOR".equals(role)) {
            if (!Objects.equals(patient.getMainDoctorId(), userId)) {
                return Result.error(403, "拒绝访问：不是您的患者");
            }
        } else if (!"ADMIN".equals(role)) {
            return Result.error(403, "拒绝访问");
        }

        patient.setName(patientDetails.getName());
        patient.setGender(patientDetails.getGender());
        patient.setAge(patientDetails.getAge());
        patient.setPhone(patientDetails.getPhone());
        patient.setHeight(patientDetails.getHeight());
        patient.setWeight(patientDetails.getWeight());
        patient.setBloodPressure(patientDetails.getBloodPressure());
        // Admin can update mainDoctorId
        if ("ADMIN".equals(role) && patientDetails.getMainDoctorId() != null) {
            patient.setMainDoctorId(patientDetails.getMainDoctorId());
        }

        return Result.success(patientRepository.save(patient));
    }

    @DeleteMapping("/patients/{id}")
    public Result<Void> deletePatient(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);

        if (!"ADMIN".equals(role)) {
            return Result.error(403, "拒绝访问：只有管理员可以删除患者");
        }

        if (!patientRepository.existsById(id)) {
            return Result.error(404, "未找到患者");
        }

        patientRepository.deleteById(id);
        return Result.success(null);
    }
    
    @GetMapping("/patients/{id}")
    public Result<Patient> getPatient(@PathVariable Long id, HttpServletRequest request) {
         String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);
        
        Patient patient = patientRepository.findById(id).orElse(null);
        if (patient == null) {
            return Result.error(404, "未找到患者");
        }

        if ("DOCTOR".equals(role)) {
            if (!Objects.equals(patient.getMainDoctorId(), userId)) {
                return Result.error(403, "拒绝访问：不是您的患者");
            }
        } else if (!"ADMIN".equals(role)) {
             // Patient can check their own profile via separate endpoint usually, but if they hit this:
             // We need to check if this patient ID matches their bound patient ID.
             // But simpler to handle in getProfile for Patient role.
             // Let's allow if they are querying themselves.
             User user = userRepository.findById(userId).orElse(null);
             if (user != null && "PATIENT".equals(user.getRole()) && Objects.equals(user.getPatientId(), id)) {
                 return Result.success(patient);
             }
             return Result.error(403, "拒绝访问");
        }

        return Result.success(patient);
    }

    // --- Health Profile (Composite View) ---

    @GetMapping("/patient/profile")
    public Result<PatientProfileDto> getMyProfile(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null || !"PATIENT".equals(user.getRole()) || user.getPatientId() == null) {
            return Result.error(400, "无效的患者用户");
        }

        return getProfileByPatientId(user.getPatientId());
    }

    @GetMapping("/patients/{id}/profile")
    public Result<PatientProfileDto> getPatientProfile(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        Patient patient = patientRepository.findById(id).orElse(null);
        if (patient == null) {
             return Result.error(404, "未找到患者");
        }

        if ("DOCTOR".equals(role)) {
             if (!Objects.equals(patient.getMainDoctorId(), userId)) {
                return Result.error(403, "拒绝访问：不是您的患者");
            }
        } else if ("PATIENT".equals(role)) {
             User user = userRepository.findById(userId).orElse(null);
             if (user == null || !Objects.equals(user.getPatientId(), id)) {
                 return Result.error(403, "拒绝访问");
             }
        }
        
        return getProfileByPatientId(id);
    }

    private Result<PatientProfileDto> getProfileByPatientId(Long patientId) {
        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null) {
            return Result.error(404, "未找到患者");
        }

        Optional<TcmResult> lastResult = tcmResultRepository.findTopByPatientIdOrderByCreatedTimeDesc(patientId);
        Optional<HealthRecord> lastRecord = healthRecordRepository.findTopByPatientIdOrderByCreateTimeDesc(patientId);

        PatientProfileDto dto = new PatientProfileDto();
        dto.setPatient(patient);
        dto.setLastTcmResult(lastResult.orElse(null));
        dto.setLastHealthRecord(lastRecord.orElse(null));

        return Result.success(dto);
    }
    
    @PutMapping("/patient/profile")
    public Result<Patient> updateMyProfile(@RequestBody Patient body, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !"PATIENT".equals(user.getRole()) || user.getPatientId() == null) {
            return Result.error(400, "无效的患者用户");
        }
        Patient patient = patientRepository.findById(user.getPatientId()).orElse(null);
        if (patient == null) {
            return Result.error(404, "未找到患者");
        }
        if (body.getPhone() != null) {
            patient.setPhone(body.getPhone());
        }
        if (body.getHeight() != null) {
            patient.setHeight(body.getHeight());
        }
        if (body.getWeight() != null) {
            patient.setWeight(body.getWeight());
        }
        if (body.getBloodPressure() != null && !body.getBloodPressure().trim().isEmpty()) {
            patient.setBloodPressure(body.getBloodPressure());
        }
        return Result.success(patientRepository.save(patient));
    }
}
