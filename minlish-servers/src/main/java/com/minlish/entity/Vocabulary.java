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
 * Time      : 13:57
 * File      : Vocabulary
 */
/**
 * Thực thể từ vựng.
 * Lưu đầy đủ thông tin để học theo flashcard và context-based learning.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "vocabularies")
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vocabulary_set_id", nullable = false)
    private VocabularySet vocabularySet;

    @Column(nullable = false)
    private String word;

    private String pronunciation;

    @Column(nullable = false)
    private String meaning;

    private String description;

    @Column(name = "example_sentence")
    private String exampleSentence;

    @Column(name = "fixed_phrase")
    private String fixedPhrase;

    @Column(name = "related_words")
    private String relatedWords;

    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
