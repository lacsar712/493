package com.example.tcm.controller;

import com.example.tcm.common.Result;
import com.example.tcm.dto.QuestionSubmitRequest;
import com.example.tcm.entity.Patient;
import com.example.tcm.entity.TcmQuestion;
import com.example.tcm.entity.TcmResult;
import com.example.tcm.entity.User;
import com.example.tcm.repository.PatientRepository;
import com.example.tcm.repository.TcmQuestionRepository;
import com.example.tcm.repository.TcmResultRepository;
import com.example.tcm.repository.UserRepository;
import com.example.tcm.security.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tcm")
public class TcmController {

    @Autowired
    private TcmQuestionRepository questionRepository;

    @Autowired
    private TcmResultRepository resultRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/questions")
    public Result<List<TcmQuestion>> getQuestions() {
        return Result.success(questionRepository.findAll());
    }

    @PostMapping("/questions")
    public Result<TcmQuestion> createQuestion(@RequestBody TcmQuestion question, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "拒绝访问");
        }
        return Result.success(questionRepository.save(question));
    }

    @PutMapping("/questions/{id}")
    public Result<TcmQuestion> updateQuestion(@PathVariable Long id, @RequestBody TcmQuestion questionDetails, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "拒绝访问");
        }

        TcmQuestion question = questionRepository.findById(id).orElse(null);
        if (question == null) {
            return Result.error(404, "未找到题目");
        }

        question.setContent(questionDetails.getContent());
        question.setOptionA(questionDetails.getOptionA());
        question.setOptionAScore(questionDetails.getOptionAScore());
        question.setOptionB(questionDetails.getOptionB());
        question.setOptionBScore(questionDetails.getOptionBScore());
        question.setOptionC(questionDetails.getOptionC());
        question.setOptionCScore(questionDetails.getOptionCScore());
        question.setDimension(questionDetails.getDimension());

        return Result.success(questionRepository.save(question));
    }

    @DeleteMapping("/questions/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String role = jwtUtil.extractRole(token);
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "拒绝访问");
        }

        if (!questionRepository.existsById(id)) {
            return Result.error(404, "未找到题目");
        }

        questionRepository.deleteById(id);
        return Result.success(null);
    }

    @PostMapping("/questions/reset")
    public Result<Void> resetQuestions() {
        questionRepository.deleteAll();
        
        List<TcmQuestion> questions = java.util.Arrays.asList(
            createQuestion("您容易疲乏吗？", "从不", 0, "有时", 5, "经常", 10, "气虚"),
            createQuestion("您说话声音低弱吗？", "从不", 0, "有时", 5, "经常", 10, "气虚"),
            createQuestion("您感到胸闷或腹胀吗？", "从不", 0, "有时", 5, "经常", 10, "痰湿"),
            createQuestion("您身体沉重不轻松吗？", "从不", 0, "有时", 5, "经常", 10, "痰湿"),
            createQuestion("您精力充沛吗？", "非常", 10, "一般", 5, "差", 0, "平和"),
            createQuestion("您睡眠质量如何？", "很好", 10, "一般", 5, "差", 0, "平和"),
            createQuestion("您容易出汗吗？", "从不", 0, "有时", 5, "经常", 10, "气虚"),
            createQuestion("您口中黏腻或痰多吗？", "从不", 0, "有时", 5, "经常", 10, "痰湿")
        );
        
        questionRepository.saveAll(questions);
        return Result.success(null);
    }

    private TcmQuestion createQuestion(String content, String a, int as, String b, int bs, String c, int cs, String dim) {
        TcmQuestion q = new TcmQuestion();
        q.setContent(content);
        q.setOptionA(a); q.setOptionAScore(as);
        q.setOptionB(b); q.setOptionBScore(bs);
        q.setOptionC(c); q.setOptionCScore(cs);
        q.setDimension(dim);
        q.setCreateTime(LocalDateTime.now());
        return q;
    }

    @PostMapping("/submit")
    public Result<TcmResult> submitQuestionnaire(@RequestBody QuestionSubmitRequest request, HttpServletRequest httpRequest) throws JsonProcessingException {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null || !"PATIENT".equals(user.getRole()) || user.getPatientId() == null) {
            return Result.error(403, "仅患者可提交问卷，或您的患者档案未创建");
        }

        List<TcmQuestion> questions = questionRepository.findAll();
        Map<String, Integer> scores = new HashMap<>();
        scores.put("平和", 0);
        scores.put("气虚", 0);
        scores.put("痰湿", 0);

        int totalScore = 0; // Just for reference if needed, but we calculate by dimension

        for (TcmQuestion q : questions) {
            String answer = request.getAnswers().get(q.getId());
            if (answer != null) {
                int score = 0;
                if ("A".equalsIgnoreCase(answer)) score = q.getOptionAScore();
                else if ("B".equalsIgnoreCase(answer)) score = q.getOptionBScore();
                else if ("C".equalsIgnoreCase(answer)) score = q.getOptionCScore();
                
                String dim = q.getDimension();
                if (dim != null) {
                     scores.put(dim, scores.getOrDefault(dim, 0) + score);
                }
                totalScore += score;
            }
        }

        // Determine Body Type based on Dimension Scores
        // Logic: Pathological dimensions (QiXu, TanShi) take precedence if score is high (>=10).
        // Otherwise, PingHe.
        
        int scoreQiXu = scores.getOrDefault("气虚", 0);
        int scoreTanShi = scores.getOrDefault("痰湿", 0);
        int scorePingHe = scores.getOrDefault("平和", 0);

        String bodyType = "平和体质"; // Default
        
        // Simple heuristic: If any pathological score is high, assign that type.
        // If multiple are high, assign the highest one.
        if (scoreQiXu >= 10 || scoreTanShi >= 10) {
            if (scoreQiXu >= scoreTanShi) {
                bodyType = "气虚体质";
            } else {
                bodyType = "痰湿体质";
            }
        }
        // If neither is high, it remains PingHe (assuming PingHe score is decent, or just default healthy)

        TcmResult result = new TcmResult();
        result.setPatientId(user.getPatientId());
        result.setBodyType(bodyType);
        result.setCreatedTime(LocalDateTime.now());
        
        // Add total to scores map for record
        scores.put("Total", totalScore);
        result.setScoreJson(objectMapper.writeValueAsString(scores));

        return Result.success(resultRepository.save(result));
    }

    @GetMapping("/results/my")
    public Result<List<TcmResult>> getMyResults(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null || user.getPatientId() == null) {
            return Result.error(400, "用户未关联患者信息");
        }

        return Result.success(resultRepository.findByPatientId(user.getPatientId()));
    }

    @GetMapping("/history")
    public Result<Map<String, Object>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long patientId,
            HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return Result.error(403, "用户不存在");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        Page<TcmResult> resultPage;

        if ("PATIENT".equals(role)) {
            if (user.getPatientId() == null) {
                return Result.error(400, "用户未关联患者信息");
            }
            if (patientId != null && !patientId.equals(user.getPatientId())) {
                return Result.error(403, "只能查询自己的体质历史");
            }
            resultPage = resultRepository.findByPatientId(user.getPatientId(), pageable);
        } else if ("DOCTOR".equals(role)) {
            if (user.getDoctorId() == null) {
                return Result.error(400, "医生信息不完整");
            }
            List<Patient> myPatients = patientRepository.findByMainDoctorId(user.getDoctorId());
            List<Long> myPatientIds = myPatients.stream()
                    .map(Patient::getId)
                    .collect(Collectors.toList());
            if (patientId != null) {
                if (!myPatientIds.contains(patientId)) {
                    return Result.error(403, "只能查询自己负责患者的体质历史");
                }
                resultPage = resultRepository.findByPatientId(patientId, pageable);
            } else {
                if (myPatientIds.isEmpty()) {
                    resultPage = Page.empty(pageable);
                } else {
                    resultPage = resultRepository.findByPatientIdIn(myPatientIds, pageable);
                }
            }
        } else if ("ADMIN".equals(role)) {
            if (patientId != null) {
                resultPage = resultRepository.findByPatientId(patientId, pageable);
            } else {
                resultPage = resultRepository.findAll(pageable);
            }
        } else {
            return Result.error(403, "无权访问");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("content", resultPage.getContent());
        response.put("totalElements", resultPage.getTotalElements());
        response.put("totalPages", resultPage.getTotalPages());
        response.put("currentPage", resultPage.getNumber());
        response.put("pageSize", resultPage.getSize());

        return Result.success(response);
    }
}
