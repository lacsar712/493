package com.example.tcm.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class QuestionSubmitRequest {
    // Map of questionId -> selectedOption ('A', 'B', 'C')
    private Map<Long, String> answers; 

    public Map<Long, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Long, String> answers) {
        this.answers = answers;
    }
}
