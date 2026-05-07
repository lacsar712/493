package com.example.tcm.controller;

import com.example.tcm.common.Result;
import com.example.tcm.dto.LoginRequest;
import com.example.tcm.dto.LoginResponse;
import com.example.tcm.entity.LoginLog;
import com.example.tcm.entity.User;
import com.example.tcm.repository.LoginLogRepository;
import com.example.tcm.repository.UserRepository;
import com.example.tcm.security.JwtUtil;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import com.example.tcm.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginLogRepository loginLogRepository;

    @Autowired
    private com.example.tcm.repository.PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return Result.error(400, "用户名已存在");
        }
        if ("PATIENT".equals(request.getRole())) {
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return Result.error(400, "姓名不能为空");
            }
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "PATIENT");
        
        // Auto-create Patient profile for new patients
        if ("PATIENT".equals(user.getRole())) {
            com.example.tcm.entity.Patient patient = new com.example.tcm.entity.Patient();
            String realName = request.getName();
            if (realName == null || realName.trim().isEmpty()) {
                realName = request.getUsername();
            }
            patient.setName(realName);
            String gender = request.getGender();
            if (gender == null || gender.trim().isEmpty()) {
                gender = "未知";
            }
            patient.setGender(gender);
            if (request.getAge() != null) {
                patient.setAge(request.getAge());
            }
            if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                patient.setPhone(request.getPhone());
            }
            if (request.getHeight() != null) {
                patient.setHeight(request.getHeight());
            }
            if (request.getWeight() != null) {
                patient.setWeight(request.getWeight());
            }
            if (request.getBloodPressure() != null && !request.getBloodPressure().trim().isEmpty()) {
                patient.setBloodPressure(request.getBloodPressure());
            }
            patient = patientRepository.save(patient);
            user.setPatientId(patient.getId());
        }
        
        return Result.success(userRepository.save(user));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            // Check if user exists first for specific error message
            User user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);
            if (user == null) {
                return Result.error(401, "账号不存在");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // Self-healing: If PATIENT has no patientId, try to link or create one
            if ("PATIENT".equals(user.getRole()) && user.getPatientId() == null) {
                com.example.tcm.entity.Patient patient = patientRepository.findByName(user.getUsername()).stream().findFirst().orElse(null);
                if (patient == null) {
                    patient = new com.example.tcm.entity.Patient();
                    patient.setName(user.getUsername());
                    patient = patientRepository.save(patient);
                }
                user.setPatientId(patient.getId());
                userRepository.save(user);
            }

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId());

            // Log login
            LoginLog log = new LoginLog();
            log.setUserId(user.getId());
            log.setUsername(user.getUsername());
            log.setRole(user.getRole());
            log.setLoginTime(LocalDateTime.now());
            log.setIp(request.getRemoteAddr());
            loginLogRepository.save(log);

            return Result.success(new LoginResponse(token, user.getRole(), user.getUsername(), user.getId(), user.getPatientId()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            return Result.error(401, "密码错误");
        }
    }
    
    @GetMapping("/logs")
    public Result<java.util.List<LoginLog>> getLoginLogs(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "拒绝访问：只有管理员可以查看日志");
        }
        
        return Result.success(loginLogRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "loginTime")));
    }
}
