package com.example.tcm.repository;

import com.example.tcm.entity.TcmQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TcmQuestionRepository extends JpaRepository<TcmQuestion, Long> {
}
