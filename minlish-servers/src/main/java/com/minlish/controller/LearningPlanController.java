package com.minlish.controller;

import com.minlish.dto.LearningPlanDTO;
import com.minlish.entity.User;
import com.minlish.service.LearningPlanService;
import com.minlish.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learning-plan")
@RequiredArgsConstructor
public class LearningPlanController {

    private final LearningPlanService learningPlanService;

    @GetMapping
    public ResponseEntity<LearningPlanDTO> getCurrentPlan() {
        User currentUser = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(learningPlanService.getCurrentPlan(currentUser));
    }

    @PutMapping
    public ResponseEntity<LearningPlanDTO> updatePlan(@RequestBody LearningPlanDTO request) {
        User currentUser = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(learningPlanService.updatePlan(currentUser, request));
    }
}
