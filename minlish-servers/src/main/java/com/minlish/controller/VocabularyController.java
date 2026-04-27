package com.minlish.controller;

import com.minlish.dto.VocabularyDTO;
import com.minlish.entity.User;
import com.minlish.entity.Vocabulary;
import com.minlish.service.VocabularyService;
import com.minlish.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 15:06
 * File      : VocabularyController
 */
@RestController
@RequestMapping("/api/vocabularies")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @PostMapping("/set/{setId}")
    public ResponseEntity<?> addVocabulary(@PathVariable("setId") Long setId, @Valid @RequestBody VocabularyDTO dto) {
        User user = SecurityUtils.getCurrentUser();
        Vocabulary vocab = vocabularyService.addVocabulary(setId, user, dto);
        return ResponseEntity.ok(vocab);
    }

    @GetMapping("/set/{setId}")
    public ResponseEntity<?> getVocabulariesBySet(@PathVariable("setId") Long setId) {
        User user = SecurityUtils.getCurrentUser();
        List<Vocabulary> list = vocabularyService.getVocabulariesBySet(setId, user);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{vocabId}")
    public ResponseEntity<?> updateVocabulary(@PathVariable("vocabId") Long vocabId, @Valid @RequestBody VocabularyDTO dto) {
        User user = SecurityUtils.getCurrentUser();
        Vocabulary vocab = vocabularyService.updateVocabulary(vocabId, user, dto);
        return ResponseEntity.ok(vocab);
    }

    @DeleteMapping("/{vocabId}")
    public ResponseEntity<?> deleteVocabulary(@PathVariable("vocabId") Long vocabId) {
        User user = SecurityUtils.getCurrentUser();
        vocabularyService.deleteVocabulary(vocabId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import/{setId}")
    public ResponseEntity<?> importCsv(@PathVariable("setId") Long setId, @RequestParam("file") MultipartFile file) {
        User user = SecurityUtils.getCurrentUser();
        vocabularyService.importCsv(setId, user, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/{setId}")
    public ResponseEntity<byte[]> exportCsv(@PathVariable("setId") Long setId) {
        User user = SecurityUtils.getCurrentUser();
        byte[] content = vocabularyService.exportCsv(setId, user);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"vocabulary-set-" + setId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(content);
    }
}