-- Set names to ensure UTF-8 handling
SET NAMES utf8mb4;

-- Create Database
CREATE DATABASE IF NOT EXISTS tcm_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tcm_db;

-- Disable foreign key checks for circular references during creation
SET FOREIGN_KEY_CHECKS = 0;

-- 1. User Table
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `role` VARCHAR(20) NOT NULL COMMENT 'ADMIN, DOCTOR, PATIENT',
    `doctor_id` BIGINT COMMENT '关联医生ID (预留)',
    `patient_id` BIGINT COMMENT '关联患者ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Patient Table
CREATE TABLE IF NOT EXISTS `patient` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL,
    `gender` VARCHAR(10) NOT NULL,
    `age` INT,
    `phone` VARCHAR(20),
    `main_doctor_id` BIGINT COMMENT '主治医生ID (关联 user.id)',
    `height` FLOAT,
    `weight` FLOAT,
    `blood_pressure` VARCHAR(20),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_patient_doctor` FOREIGN KEY (`main_doctor_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add Foreign Key to User table (now that patient table exists)
ALTER TABLE `user` ADD CONSTRAINT `fk_user_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`);

-- 3. TCM Question Table
CREATE TABLE IF NOT EXISTS `tcm_question` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `content` VARCHAR(255) NOT NULL,
    `option_a` VARCHAR(100),
    `option_a_score` INT,
    `option_b` VARCHAR(100),
    `option_b_score` INT,
    `option_c` VARCHAR(100),
    `option_c_score` INT,
    `dimension` VARCHAR(50) COMMENT '维度',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. TCM Result Table
CREATE TABLE IF NOT EXISTS `tcm_result` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `patient_id` BIGINT NOT NULL,
    `body_type` VARCHAR(50) COMMENT '平和 / 气虚 / 痰湿体质',
    `score_json` TEXT COMMENT '各维度得分JSON',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_result_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Health Record Table
CREATE TABLE IF NOT EXISTS `health_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `patient_id` BIGINT NOT NULL,
    `doctor_id` BIGINT NOT NULL,
    `visit_date` DATE,
    `complaint` TEXT COMMENT '主诉',
    `diagnosis` TEXT COMMENT '诊断',
    `body_type` VARCHAR(50),
    `suggestion` TEXT COMMENT '建议',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_record_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`),
    CONSTRAINT `fk_record_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Login Log Table
CREATE TABLE IF NOT EXISTS `login_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `username` VARCHAR(50),
    `role` VARCHAR(20),
    `login_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `ip` VARCHAR(50),
    CONSTRAINT `fk_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- Seed Data
-- Password is '123456' encoded with BCrypt
-- Hash: $2a$10$E5M3gItq2p/4m1MvvLWzVuyRjKlxjBpgkfUDD.Fopw1X9Dt//atAK
-- ==========================================

-- 1. Admin User
INSERT INTO `user` (`id`, `username`, `password`, `role`, `create_time`) 
VALUES (1, 'admin', '$2a$10$E5M3gItq2p/4m1MvvLWzVuyRjKlxjBpgkfUDD.Fopw1X9Dt//atAK', 'ADMIN', NOW());

-- 2. Doctor User
INSERT INTO `user` (`id`, `username`, `password`, `role`, `create_time`) 
VALUES (2, 'doctor1', '$2a$10$E5M3gItq2p/4m1MvvLWzVuyRjKlxjBpgkfUDD.Fopw1X9Dt//atAK', 'DOCTOR', NOW());

-- 3. Patient Profile
INSERT INTO `patient` (`id`, `name`, `gender`, `age`, `phone`, `main_doctor_id`, `height`, `weight`, `blood_pressure`, `create_time`) 
VALUES (1, '张三', '男', 30, '13800138000', 2, 175.5, 70.0, '120/80', NOW());

-- 4. Patient User (Linked to Patient Profile)
INSERT INTO `user` (`id`, `username`, `password`, `role`, `patient_id`, `create_time`) 
VALUES (3, 'patient1', '$2a$10$E5M3gItq2p/4m1MvvLWzVuyRjKlxjBpgkfUDD.Fopw1X9Dt//atAK', 'PATIENT', 1, NOW());

-- 5. TCM Questions
INSERT INTO `tcm_question` (`content`, `option_a`, `option_a_score`, `option_b`, `option_b_score`, `option_c`, `option_c_score`, `dimension`) VALUES 
('您容易疲乏吗？', '从不', 0, '有时', 5, '经常', 10, '气虚'),
('您说话声音低弱吗？', '从不', 0, '有时', 5, '经常', 10, '气虚'),
('您感到胸闷或腹胀吗？', '从不', 0, '有时', 5, '经常', 10, '痰湿'),
('您身体沉重不轻松吗？', '从不', 0, '有时', 5, '经常', 10, '痰湿'),
('您精力充沛吗？', '非常', 10, '一般', 5, '差', 0, '平和'),
('您睡眠质量如何？', '很好', 10, '一般', 5, '差', 0, '平和'),
('您容易出汗吗？', '从不', 0, '有时', 5, '经常', 10, '气虚'),
('您口中黏腻或痰多吗？', '从不', 0, '有时', 5, '经常', 10, '痰湿');

-- Indexes for performance
ALTER TABLE `patient` ADD INDEX `idx_patient_main_doctor_id` (`main_doctor_id`);
ALTER TABLE `health_record` ADD INDEX `idx_record_patient_id` (`patient_id`);
ALTER TABLE `health_record` ADD INDEX `idx_record_doctor_id` (`doctor_id`);
ALTER TABLE `login_log` ADD INDEX `idx_loginlog_user_id` (`user_id`);
ALTER TABLE `patient` ADD INDEX `idx_patient_name` (`name`);

