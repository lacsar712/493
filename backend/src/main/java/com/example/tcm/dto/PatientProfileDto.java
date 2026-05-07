package com.example.tcm.dto;

import com.example.tcm.entity.HealthRecord;
import com.example.tcm.entity.Patient;
import com.example.tcm.entity.TcmResult;
import lombok.Data;

@Data
public class PatientProfileDto {
    private Patient patient;
    private TcmResult lastTcmResult;
    private HealthRecord lastHealthRecord;

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public TcmResult getLastTcmResult() {
        return lastTcmResult;
    }

    public void setLastTcmResult(TcmResult lastTcmResult) {
        this.lastTcmResult = lastTcmResult;
    }

    public HealthRecord getLastHealthRecord() {
        return lastHealthRecord;
    }

    public void setLastHealthRecord(HealthRecord lastHealthRecord) {
        this.lastHealthRecord = lastHealthRecord;
    }
}
