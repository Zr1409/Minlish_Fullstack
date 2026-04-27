package com.minlish.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:35
 * File      : DailyStats
 */
@Data
@NoArgsConstructor
@Table(name = "daily_stats")
@Entity
public class DailyStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;

    @Column(name = "words_learned")
    private Integer wordsLearned = 0;

    @Column(name = "correct_count")
    private Integer correctCount = 0;

    @Column(name = "incorrect_count")
    private Integer incorrectCount = 0;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds = 0; // Tổng thời gian học (giây)

    @Column(name = "new_words_learned")
    private Integer newWordsLearned = 0; // Số từ mới học trong ngày

    @Column(name = "review_success_count")
    private Integer reviewSuccessCount = 0; // Số từ ôn đúng trong ngày

    @Column(name = "review_total_count")
    private Integer reviewTotalCount = 0; // Tổng số từ ôn trong ngày

    @Column(name = "retention_rate")
    private Double retentionRate = 0.0; // Tỉ lệ ghi nhớ (%)

    @Column(name = "study_sessions")
    private Integer studySessions = 0; // Số phiên học trong ngày
}
