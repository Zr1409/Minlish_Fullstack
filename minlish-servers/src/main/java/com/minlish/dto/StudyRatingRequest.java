package com.minlish.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:47
 * File      : StudyRatingRequest
 */
@Data
public class StudyRatingRequest {
    @NotNull
    private Long vocabularyId;

    @NotBlank
    private String rating; // repeat, hard, good, easy
}
