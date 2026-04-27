package com.minlish.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:15
 * File      : StudyHistory
 */
/**
 * Lịch sử học/ôn của từng từ theo từng user.
 * Dùng cho SM-2 để tính lần ôn tiếp theo.
 */
@Data
@NoArgsConstructor
@Table(name = "study_history")
@Entity
public class StudyHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private Vocabulary vocabulary;

    @Column(name = "rating", length = 20)
    private String rating;

    private Double easeFactor = 2.5;

    @Column(name = "interval_days")
    private Integer intervalDays = 1;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    @Column(name = "last_review_date")
    private LocalDate lastReviewDate;

    private Integer repetitions = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

