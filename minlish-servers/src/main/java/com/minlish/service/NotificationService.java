package com.minlish.service;

import com.minlish.dto.NotificationPreferencesDTO;
import com.minlish.entity.Notification;
import com.minlish.entity.User;

import java.util.List;

/**
 * Service quản lý thông báo: tạo thông báo, lưu kết quả phiên học và gửi nhắc học/ôn.
 */
public interface NotificationService {

    Notification createTypedNotification(User user, String notificationType, String title, String content);

    Notification createStudySummaryNotification(User user, int correct, int total);

    Notification createStudySummaryNotification(User user, int correct, int total, int timeSpentSeconds);

    List<Notification> getUnreadNotifications(User user);

    long countUnreadNotifications(User user);

    List<Notification> getRecentNotifications(User user);

    void markAsRead(Long notificationId, User user);

    void sendDailyRemindersSmartly();

    void sendUpcomingReviewRemindersSmartly();

    void sendAchievementsAndMilestones();

    NotificationPreferencesDTO getNotificationPreferences(User user);

    NotificationPreferencesDTO updateNotificationPreferences(User user, NotificationPreferencesDTO request);
}
