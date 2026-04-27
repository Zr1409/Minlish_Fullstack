package com.minlish.repository;

import com.minlish.entity.Notification;
import com.minlish.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:44
 * File      : NotificationRepository
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndIsReadFalse(User user);

        long countByUserAndIsReadFalse(User user);

    List<Notification> findTop100ByUserOrderByCreatedAtDesc(User user);

    long countByUserAndNotificationType(User user, String notificationType);

    @Query(value = "SELECT COUNT(*) FROM notifications n " +
            "WHERE n.user_id = :userId " +
            "AND n.notification_type = :notificationType " +
            "AND DATE(n.created_at) = :studyDate", nativeQuery = true)
    long countSessionSummaryByUserAndDate(@Param("userId") Long userId,
                                          @Param("notificationType") String notificationType,
                                          @Param("studyDate") LocalDate studyDate);

    boolean existsByUserAndNotificationTypeAndCreatedAtBetween(
            User user, String notificationType, LocalDateTime start, LocalDateTime end);
}
