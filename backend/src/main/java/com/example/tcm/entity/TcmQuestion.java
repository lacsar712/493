package com.example.tcm.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tcm_question")
public class TcmQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(name = "option_a")
    private String optionA;
    @Column(name = "option_a_score")
    private Integer optionAScore;

    @Column(name = "option_b")
    private String optionB;
    @Column(name = "option_b_score")
    private Integer optionBScore;

    @Column(name = "option_c")
    private String optionC;
    @Column(name = "option_c_score")
    private Integer optionCScore;

    private String dimension; // e.g., "气虚", "痰湿", "平和"

    @Column(name = "create_time")
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public Integer getOptionAScore() { return optionAScore; }
    public void setOptionAScore(Integer optionAScore) { this.optionAScore = optionAScore; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public Integer getOptionBScore() { return optionBScore; }
    public void setOptionBScore(Integer optionBScore) { this.optionBScore = optionBScore; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public Integer getOptionCScore() { return optionCScore; }
    public void setOptionCScore(Integer optionCScore) { this.optionCScore = optionCScore; }
    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
