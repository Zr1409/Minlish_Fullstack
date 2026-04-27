package com.minlish.service;

import com.minlish.dto.VocabularySetDTO;
import com.minlish.entity.User;
import com.minlish.entity.VocabularySet;

import java.util.List;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:52
 * File      : VocabularySetService
 */
public interface VocabularySetService {

    VocabularySet createSet(User user, VocabularySetDTO dto);

    List<VocabularySet> getUserSets(User user);

    VocabularySet getSetById(Long setId, User user);

    VocabularySet updateSet(Long setId, User user, VocabularySetDTO dto);

    void deleteSet(Long setId, User user);
}
