package com.minlish.service;

import com.minlish.dto.UserProfileDTO;
import com.minlish.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

/**
 * Service xử lý tài khoản người dùng: đăng ký, đăng nhập, tải hồ sơ và cập nhật hồ sơ.
 */
public interface UserService extends UserDetailsService {

    User registerUser(String email, String password, String fullName, String learningGoal, String level);

    User findOrCreateGoogleUser(String email, String fullName);

    Optional<User> findByEmail(String email);

    UserProfileDTO getUserProfile(User user);

    User updateProfile(User user, UserProfileDTO dto);

    void updatePassword(User user, String newPassword);
}
