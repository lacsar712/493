package com.example.tcm.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String role;
    private String username;
    private Long id;
    private Long patientId; // Optional, for frontend convenience

    public LoginResponse() {}

    public LoginResponse(String token, String role, String username, Long id, Long patientId) {
        this.token = token;
        this.role = role;
        this.username = username;
        this.id = id;
        this.patientId = patientId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
}
