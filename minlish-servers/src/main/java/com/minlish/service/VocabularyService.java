package com.minlish.service;

import com.minlish.dto.VocabularyDTO;
import com.minlish.entity.User;
import com.minlish.entity.Vocabulary;
import org.springframework.web.multipart.MultipartFile;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:53
 * File      : VocabularyService
 */

/**
 * Service quản lý bộ từ và từ vựng: thêm, sửa, xoá, import/export CSV.
 */

import java.util.List;

public interface VocabularyService {

    Vocabulary addVocabulary(Long setId, User user, VocabularyDTO dto);

    List<Vocabulary> getVocabulariesBySet(Long setId, User user);

    Vocabulary updateVocabulary(Long vocabId, User user, VocabularyDTO dto);

    void deleteVocabulary(Long vocabId, User user);

    void importCsv(Long setId, User user, MultipartFile file);

    byte[] exportCsv(Long setId, User user);
}