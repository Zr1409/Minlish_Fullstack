package com.minlish.repository;

import com.minlish.entity.LearningPlan;
import com.minlish.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearningPlanRepository extends JpaRepository<LearningPlan, Long> {
    Optional<LearningPlan> findByUser(User user);
}
