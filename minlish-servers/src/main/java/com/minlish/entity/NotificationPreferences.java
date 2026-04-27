package com.minlish.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 31/03/2026
 * Time      : 16:30
 * File      : NotificationPreferences
 */
/**
 * Cài đặt thông báo của người dùng.
 * Điều khiển nhắc học mỗi ngày, nhắc ôn và gửi email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notification_preferences")
public class NotificationPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "enable_daily_reminder", nullable = false)
    private Boolean enableDailyReminder = true;
    
    @Column(name = "enable_review_reminder", nullable = false)
    private Boolean enableReviewReminder = true;
    
    @Column(name = "enable_email_notification", nullable = false)
    private Boolean enableEmailNotification = true;
    
    @Column(name = "reminder_time", nullable = false)
    private LocalTime reminderTime = LocalTime.of(8, 0);
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
