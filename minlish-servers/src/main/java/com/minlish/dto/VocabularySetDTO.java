package com.minlish.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:47
 * File      : VocabularySetDTO
 */
@Data
public class VocabularySetDTO {
    private Long id;
    @NotBlank
    private String name;
    private String description;
    private String tags;
}