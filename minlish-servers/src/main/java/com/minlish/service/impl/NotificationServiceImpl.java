package com.minlish.service.impl;

import com.minlish.dto.NotificationPreferencesDTO;
import com.minlish.entity.Notification;
import com.minlish.entity.NotificationPreferences;
import com.minlish.entity.DailyStats;
import com.minlish.entity.LearningPlan;
import com.minlish.entity.User;
import com.minlish.entity.Vocabulary;
import com.minlish.repository.LearningPlanRepository;
import com.minlish.repository.NotificationPreferencesRepository;
import com.minlish.repository.NotificationRepository;
import com.minlish.repository.DailyStatsRepository;
import com.minlish.repository.UserRepository;
import com.minlish.service.EmailService;
import com.minlish.service.NotificationService;
import com.minlish.service.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final String TYPE_DAILY_REMINDER = "DAILY_STUDY_REMINDER";
    private static final String TYPE_REVIEW_DUE = "REVIEW_DUE_REMINDER";
    private static final String TYPE_ACHIEVEMENT = "ACHIEVEMENT";
    private static final String TYPE_MILESTONE = "MILESTONE";
    private static final int DEFAULT_NEW_WORDS_GOAL = 10;

    private final NotificationRepository notificationRepository;
    private final NotificationPreferencesRepository preferencesRepository;
    private final StudyService studyService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final DailyStatsRepository dailyStatsRepository;
    private final LearningPlanRepository learningPlanRepository;
    private final CacheManager cacheManager;

    /**
     * Tạo thông báo trong app (notificationType dùng để lọc / gỡ trùng lịch).
     */
    @Override
    public Notification createTypedNotification(User user, String notificationType, String title, String content) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setNotificationType(notificationType);
        notification.setMessage(title + "\n" + content);
        notification.setIsRead(false);
        Notification saved = notificationRepository.save(notification);
        evictUnreadNotificationCount(user);
        return saved;
    }

    @Override
    public Notification createStudySummaryNotification(User user, int correct, int total) {
        int accuracy = total <= 0 ? 0 : Math.round((correct * 100f) / total);
        Notification notification = createTypedNotification(
                user,
                "SESSION_SUMMARY",
                "Hoàn thành phiên học",
                "Bạn vừa học xong " + total + " từ, đúng " + correct + " từ (" + accuracy + "%).");

        syncDailySessionStats(user, correct, total, 0);
        evictStatsSummary(user);
        return notification;
    }

    @Override
    public Notification createStudySummaryNotification(User user, int correct, int total, int timeSpentSeconds) {
        int accuracy = total <= 0 ? 0 : Math.round((correct * 100f) / total);
        Notification notification = createTypedNotification(
                user,
                "SESSION_SUMMARY",
                "Hoàn thành phiên học",
                "Bạn vừa học xong " + total + " từ, đúng " + correct + " từ (" + accuracy + "%).");

        syncDailySessionStats(user, correct, total, timeSpentSeconds);
        evictStatsSummary(user);
        return notification;
    }

    private void syncDailySessionStats(User user, int correct, int total, int timeSpentSeconds) {
        LocalDate today = LocalDate.now();
        DailyStats stats = dailyStatsRepository.findByUserAndStudyDate(user, today)
                .orElseGet(() -> {
                    DailyStats newStats = new DailyStats();
                    newStats.setUser(user);
                    newStats.setStudyDate(today);
                    return newStats;
                });

        int sessionsToday = (int) notificationRepository.countSessionSummaryByUserAndDate(
                user.getId(), "SESSION_SUMMARY", today);
        stats.setStudySessions(Math.max(1, sessionsToday));

        int currentTimeSpentSeconds = stats.getTimeSpentSeconds() != null ? stats.getTimeSpentSeconds() : 0;
        int totalTimeSpentSeconds = currentTimeSpentSeconds + Math.max(0, timeSpentSeconds);
        stats.setTimeSpentSeconds(totalTimeSpentSeconds);

        int reviewSuccessCount = stats.getReviewSuccessCount() != null ? stats.getReviewSuccessCount() : 0;
        int reviewTotalCount = stats.getReviewTotalCount() != null ? stats.getReviewTotalCount() : 0;
        double retentionRate = reviewTotalCount == 0 ? 0
            : (double) reviewSuccessCount / reviewTotalCount * 100;
        stats.setRetentionRate(Math.round(retentionRate * 100) / 100.0);

        dailyStatsRepository.save(stats);
    }

    /**
     * Lấy thông báo chưa đọc của user
     */
    @Override
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalse(user);
    }

    @Override
    @Cacheable(cacheNames = "unreadNotificationCounts", key = "#user.id")
    public long countUnreadNotifications(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    public List<Notification> getRecentNotifications(User user) {
        return notificationRepository.findTop100ByUserOrderByCreatedAtDesc(user);
    }

    private void ensureTodayReviewDueNotification(User user, boolean sendEmail) {
        LocalDate today = LocalDate.now();
        NotificationPreferences prefs = preferencesRepository.findByUser(user).orElse(null);
        if (prefs == null || !Boolean.TRUE.equals(prefs.getEnableReviewReminder())) {
            return;
        }

        List<Vocabulary> due = studyService.getTodayReviewWords(user);
        if (due.isEmpty()) {
            return;
        }

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        if (notificationRepository.existsByUserAndNotificationTypeAndCreatedAtBetween(user, TYPE_REVIEW_DUE, start, end)) {
            return;
        }

        createTypedNotification(user, TYPE_REVIEW_DUE, "Từ đến hạn ôn",
                "Bạn có " + due.size() + " từ cần ôn hôm nay. Vào luyện tập để không quên kiến thức!");

        if (sendEmail && Boolean.TRUE.equals(prefs.getEnableEmailNotification())) {
            sendReviewDueEmail(user, due.size());
        }
    }

    /**
     * Đánh dấu thông báo đã đọc
     */
    @Override
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Thông báo không tồn tại"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Không có quyền");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
        evictUnreadNotificationCount(user);
    }

    /**
     * Nhắc học mỗi ngày. Gửi vào giờ reminder_time của user.
     * Chạy mỗi 1 phút để check xem có user nào cần gửi nhắc học không.
     */
    @Override
    @Scheduled(cron = "0 */1 * * * ?")
    public void sendDailyRemindersSmartly() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        int page = 0;
        Page<User> batch;
        do {
            batch = userRepository.findAll(PageRequest.of(page, 200, Sort.by("id")));
            for (User user : batch.getContent()) {
                NotificationPreferences prefs = preferencesRepository.findByUser(user)
                        .orElse(null);

                if (prefs == null || !prefs.getEnableDailyReminder()) {
                    continue;
                }

                LocalTime reminderTime = prefs.getReminderTime() != null ? prefs.getReminderTime() : LocalTime.of(8, 0);
                if (!isTimeInWindow(currentTime, reminderTime)) {
                    continue;
                }

                LocalDateTime start = today.atStartOfDay();
                LocalDateTime end = today.plusDays(1).atStartOfDay();
                if (notificationRepository.existsByUserAndNotificationTypeAndCreatedAtBetween(
                        user, TYPE_DAILY_REMINDER, start, end)) {
                    continue;
                }

                createTypedNotification(user, TYPE_DAILY_REMINDER, "Nhắc học mỗi ngày",
                    "Dành vài phút với MinLish để giữ nhịp học nhé!");

                if (Boolean.TRUE.equals(prefs.getEnableEmailNotification())) {
                    String subject = "MinLish | Đến giờ học rồi";
                    String body = "Chào bạn,\n\n"
                        + "Đây là nhắc học hằng ngày của bạn.\n"
                            + "Dành vài phút với MinLish để giữ nhịp học nhé.\n\n"
                            + "Mở ứng dụng để ôn tập ngay.";
                    sendEmailQuietly(user.getEmail(), subject, body);
                }
            }
            page++;
        } while (batch.hasNext());
    }

    /**
        * Nhắc từ đến hạn ôn. Gửi vào giờ reminder_time của user.
     * Chạy mỗi 1 phút để check.
     */
    @Override
    @Scheduled(cron = "0 */1 * * * ?")
    public void sendUpcomingReviewRemindersSmartly() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        int page = 0;
        Page<User> batch;
        do {
            batch = userRepository.findAll(PageRequest.of(page, 200, Sort.by("id")));
            for (User user : batch.getContent()) {
                NotificationPreferences prefs = preferencesRepository.findByUser(user)
                        .orElse(null);

                if (prefs == null || !prefs.getEnableReviewReminder()) {
                    continue;
                }

                LocalTime reminderTime = prefs.getReminderTime() != null ? prefs.getReminderTime() : LocalTime.of(8, 0);
                if (!isTimeInWindow(currentTime, reminderTime)) {
                    continue;
                }
                ensureTodayReviewDueNotification(user, true);
            }
            page++;
        } while (batch.hasNext());
    }

    /**
     * Achievement & milestone: kiểm tra mỗi 1 phút, tránh gửi trùng trong ngày.
     * - achievement: đạt >=10 từ mới trong ngày
     * - milestone: streak đạt bội số 7
     */
    @Override
    @Scheduled(cron = "0 */1 * * * ?")
    public void sendAchievementsAndMilestones() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        int page = 0;
        Page<User> batch;
        do {
            batch = userRepository.findAll(PageRequest.of(page, 200, Sort.by("id")));
            for (User user : batch.getContent()) {
                int newWordsGoal = resolveNewWordsGoal(user);
                DailyStats todayStats = dailyStatsRepository.findByUserAndStudyDate(user, today).orElse(null);
                int wordsToday = todayStats != null && todayStats.getNewWordsLearned() != null
                    ? todayStats.getNewWordsLearned()
                    : 0;

                if (newWordsGoal > 0 && wordsToday >= newWordsGoal
                    && !notificationRepository.existsByUserAndNotificationTypeAndCreatedAtBetween(
                        user, TYPE_ACHIEVEMENT, startOfDay, endOfDay)) {
                    createTypedNotification(
                            user,
                            TYPE_ACHIEVEMENT,
                            "Hoàn thành mục tiêu học ngày hôm nay",
                        "Bạn đã hoàn thành " + wordsToday + "/" + newWordsGoal + " từ mới hôm nay! Tuyệt vời!");
                }

                int streak = calculateStreak(user);
                if (streak >= 3 && streak % 3 == 0
                        && !notificationRepository.existsByUserAndNotificationTypeAndCreatedAtBetween(
                                user, TYPE_MILESTONE, startOfDay, endOfDay)) {
                    createTypedNotification(
                            user,
                            TYPE_MILESTONE,
                            "Chuỗi ngày học liên tiếp",
                            "Chúc mừng! Bạn đã đạt " + streak + " ngày học liên tiếp");
                }
            }
            page++;
        } while (batch.hasNext());
    }

    private int resolveNewWordsGoal(User user) {
        return learningPlanRepository.findByUser(user)
                .map(LearningPlan::getNewWordsPerDay)
                .filter(goal -> goal != null && goal > 0)
                .orElse(DEFAULT_NEW_WORDS_GOAL);
    }

    private int calculateStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate recent = findMostRecentSessionDate(user, today);
        if (recent == null) {
            return 0;
        }

        int streak = 0;
        LocalDate cursor = recent;
        int limit = 365;
        while (limit-- > 0 && hasSessionOnDate(user, cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private LocalDate findMostRecentSessionDate(User user, LocalDate start) {
        LocalDate cursor = start;
        int limit = 365;
        while (limit-- > 0) {
            if (hasSessionOnDate(user, cursor)) {
                return cursor;
            }
            cursor = cursor.minusDays(1);
        }
        return null;
    }

    private boolean hasSessionOnDate(User user, LocalDate date) {
        return notificationRepository.countSessionSummaryByUserAndDate(user.getId(), "SESSION_SUMMARY", date) > 0;
    }

    private void sendReviewDueEmail(User user, int dueCount) {
        sendEmailQuietly(
                user.getEmail(),
                "MinLish: " + dueCount + " từ đến hạn ôn",
                "Chào bạn,\n\n"
                        + "Hôm nay bạn có " + dueCount + " từ cần ôn.\n"
                        + "Đăng nhập MinLish để ôn các từ đến hạn.\n\n"
                        + "Mở ứng dụng để bắt đầu ngay.");
    }

    private boolean isTimeInWindow(LocalTime current, LocalTime target) {
        return !current.isBefore(target.minusMinutes(5)) && !current.isAfter(target.plusMinutes(5));
    }

    private void sendEmailQuietly(String to, String subject, String body) {
        try {
            emailService.sendEmail(to, subject, body);
        } catch (Exception e) {
            log.error("Failed to send email to {} with subject '{}': {}", to, subject, e.getMessage(), e);
        }
    }

    /**
     * Lấy cài đặt thông báo của user
     */
    @Override
    public NotificationPreferencesDTO getNotificationPreferences(User user) {
        NotificationPreferences prefs = preferencesRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cài đặt thông báo không tồn tại"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String reminderTimeStr = prefs.getReminderTime() != null ? prefs.getReminderTime().format(formatter) : "08:00";

        return NotificationPreferencesDTO.builder()
                .enableDailyReminder(prefs.getEnableDailyReminder() != null ? prefs.getEnableDailyReminder() : true)
                .enableReviewReminder(prefs.getEnableReviewReminder() != null ? prefs.getEnableReviewReminder() : true)
                .enableEmailNotification(
                        prefs.getEnableEmailNotification() != null ? prefs.getEnableEmailNotification() : true)
                .reminderTime(reminderTimeStr)
                .build();
    }

    /**
     * Cập nhật cài đặt thông báo của user
     */
    @Override
    public NotificationPreferencesDTO updateNotificationPreferences(User user, NotificationPreferencesDTO request) {
        NotificationPreferences prefs = preferencesRepository.findByUser(user)
                .orElse(null);

        if (prefs == null) {
            prefs = NotificationPreferences.builder()
                    .user(user)
                    .enableDailyReminder(request.getEnableDailyReminder())
                    .enableReviewReminder(request.getEnableReviewReminder())
                    .enableEmailNotification(request.getEnableEmailNotification())
                    .build();
        } else {
            prefs.setEnableDailyReminder(request.getEnableDailyReminder());
            prefs.setEnableReviewReminder(request.getEnableReviewReminder());
            prefs.setEnableEmailNotification(request.getEnableEmailNotification());
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime reminderTime = LocalTime.parse(request.getReminderTime(), formatter);
            prefs.setReminderTime(reminderTime);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Định dạng giờ không hợp lệ. Vui lòng sử dụng HH:MM (ví dụ: 08:30). Lỗi: " + e.getMessage());
        }

        prefs = preferencesRepository.save(prefs);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String reminderTimeStr = prefs.getReminderTime() != null ? prefs.getReminderTime().format(formatter) : "08:00";

        return NotificationPreferencesDTO.builder()
                .enableDailyReminder(prefs.getEnableDailyReminder())
                .enableReviewReminder(prefs.getEnableReviewReminder())
                .enableEmailNotification(prefs.getEnableEmailNotification())
                .reminderTime(reminderTimeStr)
                .build();
    }

    private void evictUnreadNotificationCount(User user) {
        evictCacheEntry("unreadNotificationCounts", user.getId());
    }

    private void evictStatsSummary(User user) {
        evictCacheEntry("statsSummaries", user.getId());
    }

    private void evictCacheEntry(String cacheName, Long userId) {
        if (userId == null) {
            return;
        }
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(userId);
        }
    }
}
