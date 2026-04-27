package com.minlish.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:38
 * File      : Notification
 */

/**
 * Thực thể thông báo của hệ thống.
 * Dùng cho nhắc học, nhắc ôn, thành tích và thông báo kết quả phiên học.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** DB column is `message` (TEXT NOT NULL); must not map as title/content. */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "notification_type", length = 50)
    private String notificationType;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
