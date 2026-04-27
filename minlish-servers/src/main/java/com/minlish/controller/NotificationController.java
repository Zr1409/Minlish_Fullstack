package com.minlish.controller;

import com.minlish.dto.NotificationPreferencesDTO;
import com.minlish.dto.StudySessionSummaryRequest;
import com.minlish.entity.User;
import com.minlish.service.NotificationService;
import com.minlish.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 15:13
 * File      : NotificationController
 */

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getUnreadNotifications() {
        User user = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        User user = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(notificationService.countUnreadNotifications(user));
    }

    /** Danh sách thông báo gần đây (đã đọc + chưa đọc), tối đa 100. */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentNotifications() {
        User user = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(notificationService.getRecentNotifications(user));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable("notificationId") Long notificationId) {
        User user = SecurityUtils.getCurrentUser();
        notificationService.markAsRead(notificationId, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/session-summary")
    public ResponseEntity<?> createSessionSummary(@Valid @RequestBody StudySessionSummaryRequest request) {
        User user = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(
                notificationService.createStudySummaryNotification(user, request.getCorrect(), request.getTotal(), request.getTimeSpentSeconds())
        );
    }

    /** Lấy cài đặt thông báo của user */
    @GetMapping("/preferences")
    public ResponseEntity<?> getNotificationPreferences() {
        User user = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(notificationService.getNotificationPreferences(user));
    }

    /** Cập nhật cài đặt thông báo */
    @PutMapping("/preferences")
    public ResponseEntity<?> updateNotificationPreferences(@Valid @RequestBody NotificationPreferencesDTO request) {
        User user = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(notificationService.updateNotificationPreferences(user, request));
    }
}
