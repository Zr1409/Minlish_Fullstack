package com.minlish.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 31/03/2026
 * Time      : 16:00
 * File      : NotificationPreferencesDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferencesDTO {
    
    @NotNull(message = "enableDailyReminder không được để trống")
    private Boolean enableDailyReminder;
    
    @NotNull(message = "enableReviewReminder không được để trống")
    private Boolean enableReviewReminder;
    
    @NotNull(message = "enableEmailNotification không được để trống")
    private Boolean enableEmailNotification;
    
    @NotNull(message = "reminderTime không được để trống")
    private String reminderTime; // Định dạng HH:MM
}
