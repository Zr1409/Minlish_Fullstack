package com.minlish.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DueReviewSetDTO {
    private Long setId;
    private String setName;
    private Integer totalDueWords;
    private List<DueReviewWordDTO> words = new ArrayList<>();
}
