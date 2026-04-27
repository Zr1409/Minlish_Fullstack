package com.minlish.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:48
 * File      : VocabularyDTO
 */
@Data
public class VocabularyDTO {
    private Long id;
    @NotBlank
    private String word;
    private String pronunciation;
    @NotBlank
    private String meaning;
    private String description;
    private String exampleSentence;
    private String fixedPhrase;
    private String relatedWords;
    private String notes;
}
