package com.example.tcm.controller;

import com.example.tcm.common.Result;
import com.example.tcm.dto.UserCreateRequest;
import com.example.tcm.entity.User;
import com.example.tcm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/create")
    public Result<User> createUser(@RequestBody UserCreateRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return Result.error(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPatientId(request.getPatientId());
        user.setDoctorId(request.getDoctorId());

        return Result.success(userRepository.save(user));
    }

    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody UserCreateRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error(404, "用户未找到");
        }

        // Only update fields if they are provided
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
             // Check if new username exists and is not this user
             User existing = userRepository.findByUsername(request.getUsername()).orElse(null);
             if (existing != null && !existing.getId().equals(id)) {
                 return Result.error(400, "用户名已存在");
             }
             user.setUsername(request.getUsername());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        
        user.setPatientId(request.getPatientId());
        user.setDoctorId(request.getDoctorId());

        return Result.success(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.error(404, "用户未找到");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return Result.error(400, "管理员账号不可删除");
        }
        try {
            userRepository.deleteById(id);
            return Result.success(null);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            String reason;
            if ("DOCTOR".equalsIgnoreCase(user.getRole())) {
                reason = "该医生账号已被患者（主治医生）或健康档案引用";
            } else {
                reason = "该账号已被其他数据引用（如登录日志、健康档案等）";
            }
            return Result.error(409, "删除失败：" + reason + "，请先解除关联后再删除");
        }
    }

    @GetMapping
    public Result<List<User>> listUsers() {
        return Result.success(userRepository.findAll());
    }
}
