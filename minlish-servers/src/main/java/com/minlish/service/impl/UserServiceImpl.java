package com.minlish.service.impl;

import com.minlish.security.CustomUserPrincipal;
import com.minlish.dto.UserProfileDTO;
import com.minlish.entity.NotificationPreferences;
import com.minlish.entity.User;
import com.minlish.repository.NotificationPreferencesRepository;
import com.minlish.repository.UserRepository;
import com.minlish.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final NotificationPreferencesRepository preferencesRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(String email, String password, String fullName, String learningGoal, String level) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã tồn tại!");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setLearningGoal(learningGoal);
        user.setLevel(level);
        user = userRepository.save(user);

        NotificationPreferences prefs = NotificationPreferences.builder()
                .user(user)
                .enableDailyReminder(true)
                .enableReviewReminder(true)
                .enableEmailNotification(true)
                .reminderTime(LocalTime.of(8, 0))
                .build();
        preferencesRepository.save(prefs);

        return user;
    }

    @Override
    public User findOrCreateGoogleUser(String email, String fullName) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setFullName((fullName == null || fullName.isBlank()) ? email.split("@")[0] : fullName);
            user.setLearningGoal("Communication");
            user.setLevel("A1");
            User saved = userRepository.save(user);

            NotificationPreferences prefs = NotificationPreferences.builder()
                    .user(saved)
                    .enableDailyReminder(true)
                    .enableReviewReminder(true)
                    .enableEmailNotification(true)
                    .reminderTime(LocalTime.of(8, 0))
                    .build();
            preferencesRepository.save(prefs);

            return saved;
        });
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User không tồn tại với email: " + email));

        return new CustomUserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user.getFullName());
    }

    @Override
    public UserProfileDTO getUserProfile(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setLearningGoal(user.getLearningGoal());
        dto.setLevel(user.getLevel());
        return dto;
    }

    @Override
    public User updateProfile(User user, UserProfileDTO dto) {
        user.setFullName(dto.getFullName());
        user.setLearningGoal(dto.getLearningGoal());
        user.setLevel(dto.getLevel());
        return userRepository.save(user);
    }

    @Override
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
