package com.minlish.service.impl;

import com.minlish.dto.StudyRatingRequest;
import com.minlish.entity.DailyStats;
import com.minlish.entity.StudyHistory;
import com.minlish.entity.User;
import com.minlish.entity.Vocabulary;
import com.minlish.repository.DailyStatsRepository;
import com.minlish.repository.StudyHistoryRepository;
import com.minlish.repository.VocabularyRepository;
import com.minlish.service.StudyService;
import com.minlish.util.SM2Util;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyServiceImpl implements StudyService {

    private final StudyHistoryRepository studyHistoryRepository;
    private final VocabularyRepository vocabularyRepository;
    private final DailyStatsRepository dailyStatsRepository;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public void processStudyResult(User user, StudyRatingRequest request) {
        Vocabulary vocabulary = vocabularyRepository.findById(request.getVocabularyId())
                .orElseThrow(() -> new RuntimeException("Từ vựng không tồn tại"));

        StudyHistory history = studyHistoryRepository.findByUserAndVocabulary(user, vocabulary)
                .orElse(new StudyHistory());
        LocalDate today = LocalDate.now();
        boolean isNewVocabulary = history.getId() == null;
        boolean reviewedTodayBefore = today.equals(history.getLastReviewDate());
        boolean wasDueBeforeReview = !isNewVocabulary
                && history.getNextReviewDate() != null
                && !history.getNextReviewDate().isAfter(today);

        if (history.getId() == null) {
            history.setUser(user);
            history.setVocabulary(vocabulary);
        }

        history.setLastReviewDate(today);

        double oldEase = history.getEaseFactor() != null ? history.getEaseFactor() : 2.5;
        int oldInterval = history.getIntervalDays() != null ? history.getIntervalDays() : 1;
        if (oldInterval < 1) {
            oldInterval = 1;
        }
        int oldReps = history.getRepetitions() != null ? history.getRepetitions() : 0;

        String rating = normalizeRating(request.getRating());
        String sm2Rating = "again".equals(rating) ? "repeat" : rating;

        // Lưu rating gốc từ UI để dễ debug và đối soát dữ liệu.
        history.setRating(rating);

        if (oldInterval > SM2Util.MAX_INTERVAL_DAYS) {
            oldInterval = SM2Util.MAX_INTERVAL_DAYS;
        }

        SM2Util.SM2Result result = SM2Util.calculate(sm2Rating, oldEase, oldInterval, oldReps);

        int intervalDays = Math.min(Math.max(1, result.getIntervalDays()), SM2Util.MAX_INTERVAL_DAYS);
        history.setEaseFactor(result.getEaseFactor());
        history.setIntervalDays(intervalDays);
        history.setRepetitions(result.getRepetitions());
        LocalDate next = today.plusDays((long) intervalDays);
        LocalDate maxMysqlDate = LocalDate.of(9999, 12, 31);
        if (next.isAfter(maxMysqlDate)) {
            next = maxMysqlDate;
        }
        history.setNextReviewDate(next);

        studyHistoryRepository.save(history);

        updateDailyStats(user, rating, isNewVocabulary, wasDueBeforeReview, reviewedTodayBefore);
        evictStatsSummary(user);
    }

    private void updateDailyStats(
            User user,
            String rating,
            boolean isNewVocabulary,
            boolean wasDueBeforeReview,
            boolean reviewedTodayBefore) {
        LocalDate today = LocalDate.now();
        DailyStats stats = dailyStatsRepository.findByUserAndStudyDate(user, today)
                .orElseGet(() -> {
                    DailyStats newStats = new DailyStats();
                    newStats.setUser(user);
                    newStats.setStudyDate(today);
                    return newStats;
                });

        int currentWordsLearned = stats.getWordsLearned() != null ? stats.getWordsLearned() : 0;
        int currentNewWordsLearned = stats.getNewWordsLearned() != null ? stats.getNewWordsLearned() : 0;
        int currentReviewSuccessCount = stats.getReviewSuccessCount() != null ? stats.getReviewSuccessCount() : 0;
        int currentReviewTotalCount = stats.getReviewTotalCount() != null ? stats.getReviewTotalCount() : 0;

        if (isNewVocabulary) {
            currentNewWordsLearned += 1;
        } else if (wasDueBeforeReview && !reviewedTodayBefore) {
            currentReviewTotalCount += 1;
            if ("good".equals(rating) || "easy".equals(rating)) {
                currentReviewSuccessCount += 1;
            }
        }

        stats.setNewWordsLearned(currentNewWordsLearned);
        stats.setWordsLearned(currentWordsLearned + 1);
        stats.setReviewSuccessCount(currentReviewSuccessCount);
        stats.setReviewTotalCount(currentReviewTotalCount);

        if ("good".equals(rating) || "easy".equals(rating)) {
            stats.setCorrectCount(stats.getCorrectCount() + 1);
        } else if ("again".equals(rating) || "hard".equals(rating)) {
            stats.setIncorrectCount(stats.getIncorrectCount() + 1);
        }

        int correctCount = stats.getReviewSuccessCount() != null ? stats.getReviewSuccessCount() : 0;
        int reviewedCount = stats.getReviewTotalCount() != null ? stats.getReviewTotalCount() : 0;
        double retentionRate = reviewedCount == 0 ? 0 : (double) correctCount / reviewedCount * 100;
        stats.setRetentionRate(Math.round(retentionRate * 100) / 100.0);

        dailyStatsRepository.save(stats);
    }

    private void evictStatsSummary(User user) {
        Cache cache = cacheManager.getCache("statsSummaries");
        if (cache != null && user.getId() != null) {
            cache.evict(user.getId());
        }
    }

    private String normalizeRating(String rating) {
        String normalized = rating == null ? "" : rating.trim().toLowerCase();
        if ("again".equals(normalized) || "hard".equals(normalized)
                || "good".equals(normalized) || "easy".equals(normalized)) {
            return normalized;
        }
        if ("repeat".equals(normalized)) {
            return "again";
        }
        throw new IllegalArgumentException("Rating không hợp lệ: " + rating);
    }

    @Override
    public List<Vocabulary> getTodayReviewWords(User user) {
        LocalDate today = LocalDate.now();
        List<StudyHistory> histories = studyHistoryRepository.findByUserAndNextReviewDateLessThanEqual(user, today);
        return histories.stream().map(StudyHistory::getVocabulary).toList();
    }

    @Override
    public List<Vocabulary> getTodayReviewWordsBySet(User user, Long setId) {
        LocalDate today = LocalDate.now();
        List<StudyHistory> histories = studyHistoryRepository.findByUserAndNextReviewDateLessThanEqual(user, today);
        return histories.stream()
                .map(StudyHistory::getVocabulary)
                .filter(v -> v.getVocabularySet() != null && v.getVocabularySet().getId().equals(setId))
                .toList();
    }

    @Override
    public List<StudyHistory> getUpcomingReviews(User user, int days) {
        LocalDate end = LocalDate.now().plusDays(days);
        return studyHistoryRepository.findByUserAndNextReviewDateLessThanEqual(user, end);
    }
}
