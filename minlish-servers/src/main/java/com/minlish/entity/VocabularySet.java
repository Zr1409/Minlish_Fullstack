package com.minlish.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 28/03/2026
 * Time      : 13:56
 * File      : VocabularySet
 */
/**
 * Thực thể bộ từ vựng.
 * Mỗi bộ thuộc về một user và chứa tên, mô tả, tags.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "vocabulary_sets")
public class VocabularySet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String description;

    private String tags; // "IELTS,Business,Travel"

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}


