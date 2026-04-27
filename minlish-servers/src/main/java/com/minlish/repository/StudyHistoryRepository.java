package com.minlish.repository;

import com.minlish.entity.StudyHistory;
import com.minlish.entity.User;
import com.minlish.entity.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:43
 * File      : StudyHistoryRepository
 */
public interface StudyHistoryRepository extends JpaRepository<StudyHistory, Long> {
    Optional<StudyHistory> findByUserAndVocabulary(User user, Vocabulary vocabulary);

    List<StudyHistory> findByUserAndNextReviewDateLessThanEqual(User user, LocalDate date);

    @Query("SELECT COUNT(DISTINCT s.vocabulary) FROM StudyHistory s WHERE s.user = :user")
    long countDistinctVocabularyByUser(@Param("user") User user);

    @Query("SELECT COUNT(DISTINCT s.vocabulary) FROM StudyHistory s WHERE s.user = :user AND CAST(s.lastReviewDate AS DATE) = :date")
    long countDistinctVocabularyByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);

        @Query("""
            SELECT COUNT(DISTINCT s.vocabulary.id)
            FROM StudyHistory s
            WHERE s.user = :user
              AND s.lastReviewDate = :date
              AND s.createdAt < :startOfDay
            """)
        long countDistinctReviewedVocabularyByUserAndDate(
            @Param("user") User user,
            @Param("date") LocalDate date,
            @Param("startOfDay") LocalDateTime startOfDay);

        @Query("""
            SELECT COUNT(DISTINCT s.vocabulary.id)
            FROM StudyHistory s
            WHERE s.user = :user
              AND s.createdAt >= :startOfDay
              AND s.createdAt < :endOfDay
            """)
        long countDistinctNewVocabularyByUserInDay(
            @Param("user") User user,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    long countByUserAndNextReviewDateLessThanEqual(User user, LocalDate date);

    long countByUserAndNextReviewDateGreaterThan(User user, LocalDate date);
}