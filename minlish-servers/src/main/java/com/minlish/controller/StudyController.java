package com.minlish.controller;

import com.minlish.dto.StudyRatingRequest;
import com.minlish.entity.User;
import com.minlish.service.StudyService;
import com.minlish.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 15:07
 * File      : StudyController
 */
@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @PostMapping("/rate")
    public ResponseEntity<?> rateVocabulary(@Valid @RequestBody StudyRatingRequest request) {
        User user = SecurityUtils.getCurrentUser();
        studyService.processStudyResult(user, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayReview() {
        User user = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(studyService.getTodayReviewWords(user));
    }
}