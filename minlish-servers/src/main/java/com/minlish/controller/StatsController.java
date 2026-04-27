package com.minlish.controller;

import com.minlish.entity.User;
import com.minlish.service.StatsService;
import com.minlish.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 15:12
 * File      : StatsController
 */

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/daily")
    public ResponseEntity<?> getDailyStats(
            @RequestParam(name = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        User user = SecurityUtils.getCurrentUser();
        if (start == null) start = LocalDate.now().minusDays(30);
        if (end == null) end = LocalDate.now();
        return ResponseEntity.ok(statsService.getDailyStats(user, start, end));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() {
        User user = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(statsService.getSummary(user));
    }

    @GetMapping("/retention-rate/daily")
    public ResponseEntity<?> getRetentionRateByDay(
            @RequestParam(name = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        User user = SecurityUtils.getCurrentUser();
        if (start == null) start = LocalDate.now().minusDays(30);
        if (end == null) end = LocalDate.now();
        return ResponseEntity.ok(statsService.getRetentionRateByDay(user, start, end));
    }

    @GetMapping("/retention-rate/weekly")
    public ResponseEntity<?> getRetentionRateByWeek(
            @RequestParam(name = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        User user = SecurityUtils.getCurrentUser();
        if (start == null) start = LocalDate.now().minusDays(90);
        if (end == null) end = LocalDate.now();
        return ResponseEntity.ok(statsService.getRetentionRateByWeek(user, start, end));
    }

    @GetMapping("/retention-rate/monthly")
    public ResponseEntity<?> getRetentionRateByMonth(
            @RequestParam(name = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        User user = SecurityUtils.getCurrentUser();
        if (start == null) start = LocalDate.now().minusMonths(12);
        if (end == null) end = LocalDate.now();
        return ResponseEntity.ok(statsService.getRetentionRateByMonth(user, start, end));
    }

    @GetMapping("/review-due-sets")
    public ResponseEntity<?> getDueReviewSets() {
        User user = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(statsService.getDueReviewSets(user));
    }
}