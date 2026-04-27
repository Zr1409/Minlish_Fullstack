package com.minlish.dto;

import lombok.Data;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:48
 * File      : UserProfileDTO
 */
@Data
public class UserProfileDTO {
    private Long id;
    private String email;
    private String fullName;
    private String learningGoal;
    private String level;
}
