package com.example.tcm.controller;

import com.example.tcm.common.Result;
import com.example.tcm.entity.HealthRecord;
import com.example.tcm.entity.Patient;
import com.example.tcm.entity.TcmResult;
import com.example.tcm.entity.User;
import com.example.tcm.repository.HealthRecordRepository;
import com.example.tcm.repository.PatientRepository;
import com.example.tcm.repository.TcmResultRepository;
import com.example.tcm.repository.UserRepository;
import com.example.tcm.security.JwtUtil;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StatsController {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private HealthRecordRepository healthRecordRepository;
    @Autowired
    private TcmResultRepository tcmResultRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        Map<String, Object> data = new HashMap<>();
        LocalDate today = LocalDate.now();

        if ("ADMIN".equals(role)) {
            long totalPatients = patientRepository.count();
            List<HealthRecord> allRecords = healthRecordRepository.findAll();
            long healthRecords = allRecords.size();
            long todayVisits = allRecords.stream().filter(r -> today.equals(r.getVisitDate())).count();
            long tcmResults = tcmResultRepository.count();

            data.put("totalPatients", totalPatients);
            data.put("healthRecords", healthRecords);
            data.put("todayVisits", todayVisits);
            data.put("tcmResults", tcmResults);

            List<TcmResult> allTcm = tcmResultRepository.findAll();
            data.put("bodyTypeDistribution", toDistribution(allTcm));
            data.put("weeklyVisits", computeWeeklyVisits(allRecords, today));
            data.put("weeklyLabels", computeWeeklyLabels(today));
        } else if ("DOCTOR".equals(role)) {
            List<Patient> myPatients = patientRepository.findByMainDoctorId(userId);
            Set<Long> patientIds = myPatients.stream().map(Patient::getId).collect(Collectors.toSet());
            List<HealthRecord> doctorRecords = healthRecordRepository.findByDoctorId(userId);

            long totalPatients = myPatients.size();
            long healthRecords = doctorRecords.size();
            long todayVisits = doctorRecords.stream().filter(r -> today.equals(r.getVisitDate())).count();

            List<TcmResult> allTcm = tcmResultRepository.findAll().stream()
                    .filter(r -> patientIds.contains(r.getPatientId()))
                    .collect(Collectors.toList());

            data.put("totalPatients", totalPatients);
            data.put("healthRecords", healthRecords);
            data.put("todayVisits", todayVisits);
            data.put("tcmResults", (long) allTcm.size());
            data.put("bodyTypeDistribution", toDistribution(allTcm));
            data.put("weeklyVisits", computeWeeklyVisits(doctorRecords, today));
            data.put("weeklyLabels", computeWeeklyLabels(today));
        } else if ("PATIENT".equals(role)) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getPatientId() == null) {
                return Result.error(400, "无效的患者用户");
            }
            Long pid = user.getPatientId();
            List<HealthRecord> records = healthRecordRepository.findByPatientId(pid);
            List<TcmResult> tcm = tcmResultRepository.findByPatientId(pid);

            long totalPatients = 1;
            long healthRecords = records.size();
            long todayVisits = records.stream().filter(r -> today.equals(r.getVisitDate())).count();
            long tcmResults = tcm.size();

            data.put("totalPatients", totalPatients);
            data.put("healthRecords", healthRecords);
            data.put("todayVisits", todayVisits);
            data.put("tcmResults", tcmResults);
            data.put("bodyTypeDistribution", toDistribution(tcm));
            data.put("weeklyVisits", computeWeeklyVisits(records, today));
            data.put("weeklyLabels", computeWeeklyLabels(today));
        } else {
            return Result.error(403, "拒绝访问");
        }

        return Result.success(data);
    }

    private List<Map<String, Object>> toDistribution(List<TcmResult> results) {
        Map<String, Long> grouped = results.stream()
                .filter(r -> r.getBodyType() != null && !r.getBodyType().trim().isEmpty())
                .collect(Collectors.groupingBy(TcmResult::getBodyType, Collectors.counting()));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, Long> e : grouped.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", e.getKey());
            item.put("value", e.getValue());
            list.add(item);
        }
        return list;
    }

    private List<Integer> computeWeeklyVisits(List<HealthRecord> records, LocalDate today) {
        List<Integer> counts = new ArrayList<>(Collections.nCopies(7, 0));
        for (HealthRecord r : records) {
            LocalDate d = r.getVisitDate();
            if (d == null) continue;
            long diff = java.time.temporal.ChronoUnit.DAYS.between(today.minusDays(6), d);
            if (diff >= 0 && diff <= 6) {
                int idx = (int) diff;
                counts.set(idx, counts.get(idx) + 1);
            }
        }
        return counts;
    }

    private List<String> computeWeeklyLabels(LocalDate today) {
        List<String> labels = new ArrayList<>(7);
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            labels.add(String.format("%d/%d", d.getMonthValue(), d.getDayOfMonth()));
        }
        return labels;
    }
}
