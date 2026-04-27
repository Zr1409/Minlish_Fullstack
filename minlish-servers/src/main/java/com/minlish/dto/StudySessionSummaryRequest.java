package com.minlish.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudySessionSummaryRequest {
    @NotNull
    @Min(0)
    private Integer correct;

    @NotNull
    @Min(0)
    private Integer total;

    @NotNull
    @Min(0)
    private Integer timeSpentSeconds;
}
