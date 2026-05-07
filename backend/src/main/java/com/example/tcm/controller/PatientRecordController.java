package com.example.tcm.controller;

import com.example.tcm.common.Result;
import com.example.tcm.entity.HealthRecord;
import com.example.tcm.entity.User;
import com.example.tcm.repository.HealthRecordRepository;
import com.example.tcm.repository.UserRepository;
import com.example.tcm.security.JwtUtil;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/records")
public class PatientRecordController {

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<List<HealthRecord>> list(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        if (!"PATIENT".equals(role)) {
            return Result.error(403, "拒绝访问");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getPatientId() == null) {
            return Result.error(400, "用户未关联患者信息");
        }
        return Result.success(healthRecordRepository.findByPatientIdOrderByVisitDateDesc(user.getPatientId()));
    }
}
