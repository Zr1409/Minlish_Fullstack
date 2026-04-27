package com.minlish.repository;

import com.minlish.entity.NotificationPreferences;
import com.minlish.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 31/03/2026
 * Time      : 16:30
 * File      : NotificationPreferencesRepository
 */
@Repository
public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, Long> {
    Optional<NotificationPreferences> findByUser(User user);
}
