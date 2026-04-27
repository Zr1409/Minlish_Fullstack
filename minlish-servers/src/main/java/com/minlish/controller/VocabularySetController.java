package com.minlish.controller;

import com.minlish.dto.VocabularySetDTO;
import com.minlish.entity.User;
import com.minlish.entity.VocabularySet;
import com.minlish.service.VocabularySetService;
import com.minlish.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 15:05
 * File      : VocabularySetController
 */
@RestController
@RequestMapping("/api/sets")
@RequiredArgsConstructor
public class VocabularySetController {

    private final VocabularySetService vocabularySetService;

    @PostMapping
    public ResponseEntity<?> createSet(@Valid @RequestBody VocabularySetDTO dto) {
        User user = SecurityUtils.getCurrentUser();
        VocabularySet set = vocabularySetService.createSet(user, dto);
        return ResponseEntity.ok(set);
    }

    @GetMapping
    public ResponseEntity<?> getUserSets() {
        User user = SecurityUtils.getCurrentUser();
        List<VocabularySet> sets = vocabularySetService.getUserSets(user);
        return ResponseEntity.ok(sets);
    }

    @GetMapping("/{setId}")
    public ResponseEntity<?> getSet(@PathVariable("setId") Long setId) {
        User user = SecurityUtils.getCurrentUser();
        VocabularySet set = vocabularySetService.getSetById(setId, user);
        return ResponseEntity.ok(set);
    }

    @PutMapping("/{setId}")
    public ResponseEntity<?> updateSet(@PathVariable("setId") Long setId, @Valid @RequestBody VocabularySetDTO dto) {
        User user = SecurityUtils.getCurrentUser();
        VocabularySet set = vocabularySetService.updateSet(setId, user, dto);
        return ResponseEntity.ok(set);
    }

    @DeleteMapping("/{setId}")
    public ResponseEntity<?> deleteSet(@PathVariable("setId") Long setId) {
        User user = SecurityUtils.getCurrentUser();
        vocabularySetService.deleteSet(setId, user);
        return ResponseEntity.noContent().build();
    }
}