package com.example.tcm.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tcm_result")
public class TcmResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "body_type")
    private String bodyType;

    @Column(name = "score_json", columnDefinition = "TEXT")
    private String scoreJson;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @PrePersist
    public void prePersist() {
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
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

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getScoreJson() {
        return scoreJson;
    }

    public void setScoreJson(String scoreJson) {
        this.scoreJson = scoreJson;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
