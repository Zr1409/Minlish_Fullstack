package com.minlish.entity;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 28/03/2026
 * Time      : 13:55
 * File      : User
 */
/**
 * Thực thể người dùng của hệ thống.
 * Lưu email, mật khẩu đã mã hoá, mục tiêu học và level hiện tại.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // Mã hoá bcrypt

    private String fullName;

    @Column(name = "learning_goal")
    private String learningGoal; // IELTS, TOEIC, Communication

    private String level; // A1, A2, B1, B2, C1, C2

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}