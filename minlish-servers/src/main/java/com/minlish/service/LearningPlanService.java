package com.minlish.service;

import com.minlish.dto.LearningPlanDTO;
import com.minlish.entity.User;

public interface LearningPlanService {
    LearningPlanDTO getCurrentPlan(User user);

    LearningPlanDTO updatePlan(User user, LearningPlanDTO request);
}
