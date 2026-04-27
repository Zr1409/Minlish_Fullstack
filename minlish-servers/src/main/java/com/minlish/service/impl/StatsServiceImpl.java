package com.minlish.service.impl;

import com.minlish.dto.DueReviewSetDTO;
import com.minlish.dto.DueReviewWordDTO;
import com.minlish.entity.DailyStats;
import com.minlish.entity.StudyHistory;
import com.minlish.entity.User;
import com.minlish.repository.DailyStatsRepository;
import com.minlish.repository.NotificationRepository;
import com.minlish.repository.StudyHistoryRepository;
import com.minlish.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private static final String TYPE_SESSION_SUMMARY = "SESSION_SUMMARY";

    private final DailyStatsRepository dailyStatsRepository;
    private final StudyHistoryRepository studyHistoryRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public List<Map<String, Object>> getDailyStats(User user, LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            return List.of();
        }

        List<DailyStats> persistedStats = dailyStatsRepository.findByUserAndStudyDateBetweenOrderByStudyDateAsc(user, start, end);
        Map<LocalDate, DailyStats> byDate = new HashMap<>();
        for (DailyStats stat : persistedStats) {
            byDate.put(stat.getStudyDate(), stat);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DailyStats stat = byDate.get(date);
            
            // Dữ liệu từ DailyStats hoặc mặc định 0
            int correctCount = stat != null && stat.getCorrectCount() != null ? stat.getCorrectCount() : 0;
            int incorrectCount = stat != null && stat.getIncorrectCount() != null ? stat.getIncorrectCount() : 0;
            int timeSpentSeconds = stat != null ? getEffectiveTimeSpentSeconds(stat) : 0;
            int newWordsLearned = stat != null && stat.getNewWordsLearned() != null ? stat.getNewWordsLearned() : 0;
            int reviewSuccessCount = stat != null && stat.getReviewSuccessCount() != null ? stat.getReviewSuccessCount() : 0;
            int reviewTotalCount = stat != null && stat.getReviewTotalCount() != null ? stat.getReviewTotalCount() : 0;
            double retentionRate = stat != null && stat.getRetentionRate() != null ? stat.getRetentionRate() : 0.0;

            if (reviewTotalCount > 0) {
                retentionRate = (double) reviewSuccessCount / reviewTotalCount * 100;
            } else if (correctCount + incorrectCount > 0) {
                retentionRate = (double) correctCount / (correctCount + incorrectCount) * 100;
            }

            // Tính accuracy cho ngày này
            int totalQuestions = correctCount + incorrectCount;
            double accuracy = totalQuestions == 0 ? 0 : (double) correctCount / totalQuestions * 100;

            Map<String, Object> row = new HashMap<>();
            row.put("studyDate", date);
            row.put("date", date.toString());
            row.put("studySessions", notificationRepository.countSessionSummaryByUserAndDate(user.getId(), TYPE_SESSION_SUMMARY, date));
            row.put("newWordsLearned", newWordsLearned);
            row.put("timeSpentSeconds", timeSpentSeconds);
            row.put("correctCount", correctCount);
            row.put("incorrectCount", incorrectCount);
            row.put("accuracy", Math.round(accuracy * 100) / 100.0);
            row.put("retentionRate", Math.round(retentionRate * 100) / 100.0);
            result.add(row);
        }

        return result;
    }

    @Override
    @Cacheable(cacheNames = "statsSummaries", key = "#user.id")
    public Map<String, Object> getSummary(User user) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        
        Map<String, Object> summary = new HashMap<>();
        
        List<DailyStats> allStats = dailyStatsRepository.findByUserOrderByStudyDateAsc(user);

        // Tổng số từ đã học: đếm số từ vựng duy nhất, không cộng thêm khi ôn lại từ cũ.
        long distinctWords = studyHistoryRepository.countDistinctVocabularyByUser(user);
        summary.put("totalWords", distinctWords);

        // Tổng số lượt học: đếm theo session summary notifications, tức 1 notification = 1 phiên học
        long totalStudyRounds = notificationRepository.countByUserAndNotificationType(user, TYPE_SESSION_SUMMARY);
        if (totalStudyRounds == 0) {
            totalStudyRounds = dailyStatsRepository.sumTotalStudyRoundsByUser(user);
        }
        summary.put("totalStudyRounds", totalStudyRounds);

        // Chuỗi học liên tục
        int streak = calculateStreak(user);
        summary.put("streakDays", streak);
        
        // Tính tổng thời gian học từ TẤT CẢ ngày
        int totalTimeSpentAllTime = allStats.stream()
            .mapToInt(this::getEffectiveTimeSpentSeconds)
            .sum();
        summary.put("totalTimeSpent", totalTimeSpentAllTime);
        
        // Tính tổng ngày học
        long totalStudyDays = allStats.stream()
            .filter(s -> (s.getStudySessions() != null && s.getStudySessions() > 0)
                || (s.getNewWordsLearned() != null && s.getNewWordsLearned() > 0)
                || (s.getCorrectCount() != null && s.getCorrectCount() > 0)
                || (s.getIncorrectCount() != null && s.getIncorrectCount() > 0))
                .count();
        summary.put("totalStudyDays", totalStudyDays);

        // Thống kê 30 ngày gần nhất
        List<DailyStats> recentStats = allStats.stream()
                .filter(s -> !s.getStudyDate().isBefore(thirtyDaysAgo))
                .collect(Collectors.toList());
        
        int totalCorrect = recentStats.stream().mapToInt(s -> s.getCorrectCount() != null ? s.getCorrectCount() : 0).sum();
        int totalIncorrect = recentStats.stream().mapToInt(s -> s.getIncorrectCount() != null ? s.getIncorrectCount() : 0).sum();
        int timeSpent30Days = recentStats.stream().mapToInt(this::getEffectiveTimeSpentSeconds).sum();
        int totalNewWords = recentStats.stream().mapToInt(s -> s.getNewWordsLearned() != null ? s.getNewWordsLearned() : 0).sum();
        
        // Độ chính xác
        int total = totalCorrect + totalIncorrect;
        double accuracy = total == 0 ? 0 : (double) totalCorrect / total * 100;
        summary.put("accuracy", Math.round(accuracy));
        
        // Tỷ lệ ghi nhớ (dựa trên số từ chưa hết hạn ôn tập)
        long reviewSuccessTotal = dailyStatsRepository.sumReviewSuccessCountByUser(user);
        long reviewTotalCount = dailyStatsRepository.sumReviewTotalCountByUser(user);
        long retained = studyHistoryRepository.countByUserAndNextReviewDateGreaterThan(user, today);
        double retentionRate = reviewTotalCount == 0
            ? (distinctWords == 0 ? 0 : (double) retained / distinctWords * 100)
            : (double) reviewSuccessTotal / reviewTotalCount * 100;
        summary.put("retentionRate", Math.round(retentionRate));
        
        // Thống kê thêm từ 30 ngày
        summary.put("last30DaysTimeSpent", timeSpent30Days);
        summary.put("last30DaysNewWords", totalNewWords);
        summary.put("last30DaysStudyDays", (int) recentStats.stream()
            .filter(s -> (s.getStudySessions() != null && s.getStudySessions() > 0)
                || (s.getNewWordsLearned() != null && s.getNewWordsLearned() > 0)
                || (s.getCorrectCount() != null && s.getCorrectCount() > 0)
                || (s.getIncorrectCount() != null && s.getIncorrectCount() > 0))
                .count());

        // Ước tính mức độ
        summary.put("levelEstimate", estimateLevel(distinctWords));

        return summary;
    }

    @Override
    public List<Map<String, Object>> getRetentionRateByDay(User user, LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            return List.of();
        }

        List<DailyStats> stats = dailyStatsRepository.findByUserAndStudyDateBetweenOrderByStudyDateAsc(user, start, end);
        
        return stats.stream()
                .map(stat -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("date", stat.getStudyDate().toString());
                    row.put("retentionRate", stat.getRetentionRate() != null ? stat.getRetentionRate() : 0.0);
                    row.put("newWordsLearned", stat.getNewWordsLearned() != null ? stat.getNewWordsLearned() : 0);
                    row.put("timeSpentSeconds", getEffectiveTimeSpentSeconds(stat));
                    return row;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getRetentionRateByWeek(User user, LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            return List.of();
        }

        List<DailyStats> stats = dailyStatsRepository.findByUserAndStudyDateBetweenOrderByStudyDateAsc(user, start, end);
        
        // Nhóm theo tuần
        Map<String, List<DailyStats>> byWeek = stats.stream()
                .collect(Collectors.groupingBy(stat -> {
                    int week = stat.getStudyDate().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                    int year = stat.getStudyDate().get(IsoFields.WEEK_BASED_YEAR);
                    return year + "-W" + String.format("%02d", week);
                }));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<DailyStats>> entry : byWeek.entrySet()) {
            List<DailyStats> weekStats = entry.getValue();
            
            double avgRetention = weekStats.stream()
                    .mapToDouble(s -> s.getRetentionRate() != null ? s.getRetentionRate() : 0.0)
                    .average()
                    .orElse(0.0);
            
            int totalNewWords = weekStats.stream()
                    .mapToInt(s -> s.getNewWordsLearned() != null ? s.getNewWordsLearned() : 0)
                    .sum();
            
            int totalTimeSpent = weekStats.stream()
                    .mapToInt(this::getEffectiveTimeSpentSeconds)
                    .sum();

            Map<String, Object> row = new HashMap<>();
            row.put("week", entry.getKey());
            row.put("startDate", weekStats.get(0).getStudyDate().toString());
            row.put("endDate", weekStats.get(weekStats.size() - 1).getStudyDate().toString());
            row.put("retentionRate", Math.round(avgRetention * 100) / 100.0);
            row.put("newWordsLearned", totalNewWords);
            row.put("timeSpentSeconds", totalTimeSpent);
            result.add(row);
        }

        return result.stream()
                .sorted((a, b) -> ((String) a.get("week")).compareTo((String) b.get("week")))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getRetentionRateByMonth(User user, LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            return List.of();
        }

        List<DailyStats> stats = dailyStatsRepository.findByUserAndStudyDateBetweenOrderByStudyDateAsc(user, start, end);
        
        // Nhóm theo tháng
        Map<String, List<DailyStats>> byMonth = stats.stream()
                .collect(Collectors.groupingBy(stat -> {
                    int year = stat.getStudyDate().getYear();
                    int month = stat.getStudyDate().getMonthValue();
                    return year + "-" + String.format("%02d", month);
                }));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<DailyStats>> entry : byMonth.entrySet()) {
            List<DailyStats> monthStats = entry.getValue();
            
            double avgRetention = monthStats.stream()
                    .mapToDouble(s -> s.getRetentionRate() != null ? s.getRetentionRate() : 0.0)
                    .average()
                    .orElse(0.0);
            
            int totalNewWords = monthStats.stream()
                    .mapToInt(s -> s.getNewWordsLearned() != null ? s.getNewWordsLearned() : 0)
                    .sum();
            
            int totalTimeSpent = monthStats.stream()
                    .mapToInt(this::getEffectiveTimeSpentSeconds)
                    .sum();

            Map<String, Object> row = new HashMap<>();
            row.put("month", entry.getKey());
            row.put("startDate", monthStats.get(0).getStudyDate().toString());
            row.put("endDate", monthStats.get(monthStats.size() - 1).getStudyDate().toString());
            row.put("retentionRate", Math.round(avgRetention * 100) / 100.0);
            row.put("newWordsLearned", totalNewWords);
            row.put("timeSpentSeconds", totalTimeSpent);
            result.add(row);
        }

        return result.stream()
                .sorted((a, b) -> ((String) a.get("month")).compareTo((String) b.get("month")))
                .collect(Collectors.toList());
    }

    @Override
    public List<DueReviewSetDTO> getDueReviewSets(User user) {
        LocalDate today = LocalDate.now();
        List<StudyHistory> dueHistories = studyHistoryRepository.findByUserAndNextReviewDateLessThanEqual(user, today);

        Map<Long, DueReviewSetDTO> grouped = new LinkedHashMap<>();

        for (StudyHistory history : dueHistories) {
            if (history.getVocabulary() == null || history.getVocabulary().getVocabularySet() == null) {
                continue;
            }

            Long setId = history.getVocabulary().getVocabularySet().getId();
            String setName = history.getVocabulary().getVocabularySet().getName();

            DueReviewSetDTO setDto = grouped.computeIfAbsent(setId, key -> {
                DueReviewSetDTO dto = new DueReviewSetDTO();
                dto.setSetId(key);
                dto.setSetName(setName);
                dto.setTotalDueWords(0);
                return dto;
            });

            DueReviewWordDTO wordDto = new DueReviewWordDTO();
            wordDto.setVocabularyId(history.getVocabulary().getId());
            wordDto.setWord(history.getVocabulary().getWord());
            wordDto.setNextReviewDate(history.getNextReviewDate());
            wordDto.setLastReviewDate(history.getLastReviewDate());

            long overdueDays = 0;
            if (history.getNextReviewDate() != null && history.getNextReviewDate().isBefore(today)) {
                overdueDays = java.time.temporal.ChronoUnit.DAYS.between(history.getNextReviewDate(), today);
            }
            wordDto.setOverdueDays(overdueDays);

            setDto.getWords().add(wordDto);
            setDto.setTotalDueWords(setDto.getWords().size());
        }

        return grouped.values().stream()
                .sorted(Comparator.comparing(DueReviewSetDTO::getTotalDueWords, Comparator.nullsFirst(Integer::compareTo)).reversed())
                .collect(Collectors.toList());
    }


    private int calculateStreak(User user) {
        LocalDate today = LocalDate.now();
        List<DailyStats> allStats = dailyStatsRepository.findByUserOrderByStudyDateAsc(user);
        Map<LocalDate, DailyStats> statsByDate = new HashMap<>();
        for (DailyStats stat : allStats) {
            statsByDate.put(stat.getStudyDate(), stat);
        }

        LocalDate recent = null;
        LocalDate cursor = today;
        int lookbackLimit = 365;
        while (lookbackLimit-- > 0) {
            if (hasStudyActivity(statsByDate.get(cursor))) {
                recent = cursor;
                break;
            }
            cursor = cursor.minusDays(1);
        }
        if (recent == null) {
            return 0;
        }

        int streak = 0;
        cursor = recent;
        lookbackLimit = 365;
        while (lookbackLimit-- > 0 && hasStudyActivity(statsByDate.get(cursor))) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private boolean hasStudyActivity(DailyStats stat) {
        if (stat == null) {
            return false;
        }

        return (stat.getStudySessions() != null && stat.getStudySessions() > 0)
                || (stat.getNewWordsLearned() != null && stat.getNewWordsLearned() > 0)
                || (stat.getCorrectCount() != null && stat.getCorrectCount() > 0)
                || (stat.getIncorrectCount() != null && stat.getIncorrectCount() > 0)
            || getEffectiveTimeSpentSeconds(stat) > 0;
    }

    private String estimateLevel(long totalWords) {
        if (totalWords < 10) {
            return "Beginner";
        }
        if (totalWords < 20) {
            return "Intermediate";
        }
        return "Advanced";
    }

    private int getEffectiveTimeSpentSeconds(DailyStats stat) {
        return stat.getTimeSpentSeconds() != null ? stat.getTimeSpentSeconds() : 0;
    }
}
