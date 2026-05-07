package com.example.tcm.exception;

import com.example.tcm.common.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldLabels = new HashMap<>();
        fieldLabels.put("doctorId", "医生ID");
        fieldLabels.put("patientId", "患者ID");
        fieldLabels.put("visitDate", "就诊日期");
        fieldLabels.put("bodyType", "体质类型");
        fieldLabels.put("complaint", "主诉");
        fieldLabels.put("diagnosis", "诊断");
        fieldLabels.put("suggestion", "建议");
        fieldLabels.put("name", "姓名");
        fieldLabels.put("gender", "性别");
        fieldLabels.put("age", "年龄");
        fieldLabels.put("phone", "电话");
        fieldLabels.put("height", "身高");
        fieldLabels.put("weight", "体重");
        fieldLabels.put("bloodPressure", "血压");
        fieldLabels.put("mainDoctorId", "主治医生ID");

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> {
                    String field = err.getField();
                    String label = fieldLabels.getOrDefault(field, field);
                    String msg = err.getDefaultMessage();
                    if ("不能为null".equals(msg)) {
                        msg = "不能为空";
                    }
                    return label + msg;
                })
                .collect(Collectors.joining("；"));
        return Result.error(400, message.isEmpty() ? "参数校验失败" : message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> {
                    String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "";
                    String msg = v.getMessage();
                    if ("不能为null".equals(msg)) msg = "不能为空";
                    return path + ": " + msg;
                })
                .reduce((a, b) -> a + "；" + b)
                .orElse("参数校验失败");
        return Result.error(400, message);
    }
}
