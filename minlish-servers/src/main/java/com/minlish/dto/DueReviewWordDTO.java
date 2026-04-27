package com.minlish.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DueReviewWordDTO {
    private Long vocabularyId;
    private String word;
    private LocalDate nextReviewDate;
    private LocalDate lastReviewDate;
    private Long overdueDays;
}
