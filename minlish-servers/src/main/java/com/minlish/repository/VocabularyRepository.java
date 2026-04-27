package com.minlish.repository;

import com.minlish.entity.Vocabulary;
import com.minlish.entity.VocabularySet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:42
 * File      : VocabularyRepository
 */
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {
    List<Vocabulary> findByVocabularySet(VocabularySet vocabularySet);
    List<Vocabulary> findByVocabularySetId(Long vocabularySetId);
    List<Vocabulary> findByIdIn(List<Long> ids);
}
