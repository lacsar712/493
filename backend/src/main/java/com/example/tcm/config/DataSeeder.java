package com.example.tcm.config;

import com.example.tcm.entity.HealthRecord;
import com.example.tcm.entity.Patient;
import com.example.tcm.entity.User;
import com.example.tcm.repository.HealthRecordRepository;
import com.example.tcm.repository.PatientRepository;
import com.example.tcm.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (healthRecordRepository.count() > 0) {
            return;
        }

        System.out.println("No health records found. Seeding data...");

        // Ensure we have a patient and a doctor
        // In init.sql: Patient ID 1 (Zhang San), User ID 2 (doctor1)
        Optional<Patient> patientOpt = patientRepository.findById(1L);
        Optional<User> doctorOpt = userRepository.findByUsername("doctor1");

        if (!patientOpt.isPresent() || !doctorOpt.isPresent()) {
            System.out.println("Missing patient(id=1) or doctor(username=doctor1). Skipping seed.");
            return;
        }

        Patient patient = patientOpt.get();
        User doctor = doctorOpt.get();

        // Create sample records
        HealthRecord r1 = new HealthRecord();
        r1.setPatientId(patient.getId());
        r1.setDoctorId(doctor.getId());
        r1.setVisitDate(LocalDate.now().minusDays(10));
        r1.setComplaint("最近感觉身体疲乏，食欲不振，大便稀溏。");
        r1.setDiagnosis("气虚体质，脾胃虚弱");
        r1.setBodyType("气虚体质");
        r1.setSuggestion("建议服用补中益气汤，注意休息，避免过度劳累。饮食上多吃山药、小米等健脾食物。");

        HealthRecord r2 = new HealthRecord();
        r2.setPatientId(patient.getId());
        r2.setDoctorId(doctor.getId());
        r2.setVisitDate(LocalDate.now().minusDays(5));
        r2.setComplaint("失眠多梦，口干舌燥，手心发热。");
        r2.setDiagnosis("阴虚火旺，心肾不交");
        r2.setBodyType("阴虚体质");
        r2.setSuggestion("建议服用六味地黄丸，饮食清淡，少吃辛辣。保持心情舒畅，睡前泡脚。");

        HealthRecord r3 = new HealthRecord();
        r3.setPatientId(patient.getId());
        r3.setDoctorId(doctor.getId());
        r3.setVisitDate(LocalDate.now().minusDays(2));
        r3.setComplaint("关节酸痛，遇冷加重，舌苔白腻。");
        r3.setDiagnosis("风寒湿痹");
        r3.setBodyType("痰湿体质");
        r3.setSuggestion("建议艾灸治疗，注意保暖，适当运动。可配合理疗缓解症状。");

        healthRecordRepository.saveAll(Arrays.asList(r1, r2, r3));
        System.out.println("Seeded 3 health records successfully.");
    }
}
