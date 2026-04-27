package com.minlish.service.impl;

import com.minlish.dto.LearningPlanDTO;
import com.minlish.entity.DailyStats;
import com.minlish.entity.LearningPlan;
import com.minlish.entity.User;
import com.minlish.repository.DailyStatsRepository;
import com.minlish.repository.LearningPlanRepository;
import com.minlish.repository.StudyHistoryRepository;
import com.minlish.service.LearningPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LearningPlanServiceImpl implements LearningPlanService {

    private static final int DEFAULT_NEW_WORDS_PER_DAY = 10;

    private final LearningPlanRepository learningPlanRepository;
    private final DailyStatsRepository dailyStatsRepository;
    private final StudyHistoryRepository studyHistoryRepository;

    @Override
    public LearningPlanDTO getCurrentPlan(User user) {
        LearningPlan plan = learningPlanRepository.findByUser(user).orElse(null);
        int newWordsTarget = plan != null && plan.getNewWordsPerDay() != null
                ? plan.getNewWordsPerDay()
                : DEFAULT_NEW_WORDS_PER_DAY;
        return toDtoWithTodayProgress(newWordsTarget, user);
    }

    @Override
    public LearningPlanDTO updatePlan(User user, LearningPlanDTO request) {
        LearningPlan plan = learningPlanRepository.findByUser(user)
                .orElseGet(() -> {
                    LearningPlan newPlan = new LearningPlan();
                    newPlan.setUser(user);
                    newPlan.setNewWordsPerDay(DEFAULT_NEW_WORDS_PER_DAY);
                    return newPlan;
                });

        int requestedNewWords = request != null && request.getNewWordsPerDay() != null
                ? request.getNewWordsPerDay()
                : plan.getNewWordsPerDay();

        plan.setNewWordsPerDay(normalizeTarget(requestedNewWords));

        LearningPlan saved = learningPlanRepository.save(plan);
        return toDtoWithTodayProgress(saved.getNewWordsPerDay(), user);
    }

    private LearningPlanDTO toDtoWithTodayProgress(int newWordsTarget, User user) {
        LocalDate today = LocalDate.now();
        DailyStats todayStats = dailyStatsRepository.findByUserAndStudyDate(user, today).orElse(null);

        int todayNewWords = todayStats != null && todayStats.getNewWordsLearned() != null
                ? todayStats.getNewWordsLearned()
                : 0;
        int todayReviewWords = todayStats != null && todayStats.getReviewTotalCount() != null
            ? todayStats.getReviewTotalCount()
            : 0;
        int dueReviewWords = (int) studyHistoryRepository.countByUserAndNextReviewDateLessThanEqual(user, today);

        LearningPlanDTO dto = new LearningPlanDTO();
        dto.setNewWordsPerDay(newWordsTarget);
        dto.setTodayNewWordsLearned(todayNewWords);
        dto.setTodayReviewWordsLearned(todayReviewWords);
        dto.setTodayReviewWordsDue(dueReviewWords);
        return dto;
    }

    private int normalizeTarget(Integer value) {
        int normalized = value == null ? 0 : value;
        if (normalized < 0) {
            return 0;
        }
        return Math.min(normalized, 500);
    }
}
