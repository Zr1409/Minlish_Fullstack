package com.minlish.repository;

import com.minlish.entity.DailyStats;
import com.minlish.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:43
 * File      : DailyStatsRepository
 */
public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {
    Optional<DailyStats> findByUserAndStudyDate(User user, LocalDate date);

    List<DailyStats> findByUserAndStudyDateBetweenOrderByStudyDateAsc(User user, LocalDate start, LocalDate end);

    List<DailyStats> findByUserOrderByStudyDateAsc(User user);

    @Query("SELECT COALESCE(SUM(d.wordsLearned), 0) FROM DailyStats d WHERE d.user = :user")
    long sumWordsLearnedByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(d.studySessions), 0) FROM DailyStats d WHERE d.user = :user")
    long sumTotalStudyRoundsByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(d.newWordsLearned), 0) FROM DailyStats d WHERE d.user = :user")
    long sumNewWordsLearnedByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(d.reviewSuccessCount), 0) FROM DailyStats d WHERE d.user = :user")
    long sumReviewSuccessCountByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(d.reviewTotalCount), 0) FROM DailyStats d WHERE d.user = :user")
    long sumReviewTotalCountByUser(@Param("user") User user);
}
