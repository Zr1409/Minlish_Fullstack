package com.minlish.controller;

import com.minlish.dto.UserProfileDTO;
import com.minlish.entity.User;
import com.minlish.service.UserService;
import com.minlish.util.SecurityUtils;
import com.minlish.dto.ChangePasswordRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 15:03
 * File      : UserController
 */

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        User currentUser = SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(userService.getUserProfile(currentUser));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserProfileDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        User updated = userService.updateProfile(currentUser, dto);
        return ResponseEntity.ok(userService.getUserProfile(updated));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu cũ không đúng");
        }
        userService.updatePassword(currentUser, request.getNewPassword());
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }
}