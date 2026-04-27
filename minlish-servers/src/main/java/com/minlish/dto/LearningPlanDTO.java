package com.minlish.dto;

import lombok.Data;

/**
 * DTO cho cấu hình kế hoạch học hằng ngày và tiến độ trong ngày.
 */
@Data
public class LearningPlanDTO {
    private Integer newWordsPerDay;

    private Integer todayNewWordsLearned;
    private Integer todayReviewWordsLearned;
    private Integer todayReviewWordsDue;
}
