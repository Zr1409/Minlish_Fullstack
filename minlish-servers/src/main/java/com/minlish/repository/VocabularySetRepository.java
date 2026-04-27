package com.minlish.repository;

import com.minlish.entity.User;
import com.minlish.entity.VocabularySet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:41
 * File      : VocabularySetRepository
 */
public interface VocabularySetRepository extends JpaRepository<VocabularySet, Long> {
    List<VocabularySet> findByUser(User user);
}
