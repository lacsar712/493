package com.example.tcm.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

@Data
@Entity
@Table(name = "patient")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 10)
    private String gender;

    @Min(1)
    @Max(120)
    private Integer age;

    @Size(max = 20)
    @Pattern(regexp = "^[0-9\\-+]{6,20}$", message = "电话格式不正确")
    private String phone;

    @Column(name = "main_doctor_id")
    private Long mainDoctorId; // Keeping as ID for simplicity in JPA queries for now

    @Transient
    private String doctorName;

    @PositiveOrZero
    private Float height;
    @PositiveOrZero
    private Float weight;
    
    @Column(name = "blood_pressure")
    @Pattern(regexp = "^(\\d{2,3})/(\\d{2,3})$", message = "血压格式如 120/80")
    private String bloodPressure;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Long getMainDoctorId() { return mainDoctorId; }
    public void setMainDoctorId(Long mainDoctorId) { this.mainDoctorId = mainDoctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public Float getHeight() { return height; }
    public void setHeight(Float height) { this.height = height; }
    public Float getWeight() { return weight; }
    public void setWeight(Float weight) { this.weight = weight; }
    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
