package com.example.tcm.controller;

import com.example.tcm.common.Result;
import com.example.tcm.entity.HealthRecord;
import com.example.tcm.repository.HealthRecordRepository;
import com.example.tcm.security.JwtUtil;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/records")
public class AdminRecordController {

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public Result<List<HealthRecord>> list(@RequestParam(required = false) Long patientId, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);

        if (!"ADMIN".equals(role)) {
            return Result.error(403, "拒绝访问");
        }

        if (patientId != null) {
            return Result.success(healthRecordRepository.findByPatientId(patientId));
        }
        return Result.success(healthRecordRepository.findAll());
    }
}
