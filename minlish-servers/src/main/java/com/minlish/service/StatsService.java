package com.minlish.service;

import com.minlish.dto.DueReviewSetDTO;
import com.minlish.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:55
 * File      : StatsService
 */

/**
 * Service thống kê tiến độ học tập: dashboard, biểu đồ, retention và mức độ người học.
 */

public interface StatsService {

    List<Map<String, Object>> getDailyStats(User user, LocalDate start, LocalDate end);

    Map<String, Object> getSummary(User user);

    List<Map<String, Object>> getRetentionRateByDay(User user, LocalDate start, LocalDate end);

    List<Map<String, Object>> getRetentionRateByWeek(User user, LocalDate start, LocalDate end);

    List<Map<String, Object>> getRetentionRateByMonth(User user, LocalDate start, LocalDate end);

    List<DueReviewSetDTO> getDueReviewSets(User user);
}