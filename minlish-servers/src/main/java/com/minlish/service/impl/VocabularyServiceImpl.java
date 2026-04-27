package com.minlish.service.impl;

import com.minlish.dto.VocabularyDTO;
import com.minlish.entity.User;
import com.minlish.entity.Vocabulary;
import com.minlish.entity.VocabularySet;
import com.minlish.repository.VocabularyRepository;
import com.minlish.service.VocabularyService;
import com.minlish.service.VocabularySetService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabularyServiceImpl implements VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final VocabularySetService vocabularySetService;

    @Override
    public Vocabulary addVocabulary(Long setId, User user, VocabularyDTO dto) {
        VocabularySet set = vocabularySetService.getSetById(setId, user);
        Vocabulary vocab = new Vocabulary();
        vocab.setVocabularySet(set);
        vocab.setWord(dto.getWord());
        vocab.setPronunciation(dto.getPronunciation());
        vocab.setMeaning(dto.getMeaning());
        vocab.setDescription(dto.getDescription());
        vocab.setExampleSentence(dto.getExampleSentence());
        vocab.setFixedPhrase(dto.getFixedPhrase());
        vocab.setRelatedWords(dto.getRelatedWords());
        vocab.setNotes(dto.getNotes());
        return vocabularyRepository.save(vocab);
    }

    @Override
    public List<Vocabulary> getVocabulariesBySet(Long setId, User user) {
        VocabularySet set = vocabularySetService.getSetById(setId, user);
        return vocabularyRepository.findByVocabularySet(set);
    }

    @Override
    public Vocabulary updateVocabulary(Long vocabId, User user, VocabularyDTO dto) {
        Vocabulary vocab = vocabularyRepository.findById(vocabId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Từ vựng không tồn tại"));
        if (!vocab.getVocabularySet().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền sửa từ này");
        }
        vocab.setWord(dto.getWord());
        vocab.setPronunciation(dto.getPronunciation());
        vocab.setMeaning(dto.getMeaning());
        vocab.setDescription(dto.getDescription());
        vocab.setExampleSentence(dto.getExampleSentence());
        vocab.setFixedPhrase(dto.getFixedPhrase());
        vocab.setRelatedWords(dto.getRelatedWords());
        vocab.setNotes(dto.getNotes());
        return vocabularyRepository.save(vocab);
    }

    @Override
    public void deleteVocabulary(Long vocabId, User user) {
        Vocabulary vocab = vocabularyRepository.findById(vocabId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Từ vựng không tồn tại"));
        if (!vocab.getVocabularySet().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa từ này");
        }
        vocabularyRepository.delete(vocab);
    }

    @Override
    public void importCsv(Long setId, User user, MultipartFile file) {
        VocabularySet set = vocabularySetService.getSetById(setId, user);
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();

        try {
            List<Vocabulary> vocabularies = filename.endsWith(".xlsx") || filename.endsWith(".xls")
                    ? parseExcel(file, set)
                    : parseCsv(file, set);

            if (vocabularies.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File không có dòng dữ liệu hợp lệ");
            }
            vocabularyRepository.saveAll(vocabularies);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException statusException) {
                throw statusException;
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lỗi import file từ vựng", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv(Long setId, User user) {
        try {
            vocabularySetService.getSetById(setId, user);
            List<Vocabulary> words = vocabularyRepository.findByVocabularySetId(setId);

            StringBuilder csv = new StringBuilder();
            csv.append('\uFEFF');
            csv.append("word,pronunciation,meaning,description,example_sentence,fixed_phrase,related_words,notes\n");
            for (Vocabulary v : words) {
                csv.append(escapeCsv(v.getWord())).append(',')
                        .append(escapeCsv(v.getPronunciation())).append(',')
                        .append(escapeCsv(v.getMeaning())).append(',')
                        .append(escapeCsv(v.getDescription())).append(',')
                        .append(escapeCsv(v.getExampleSentence())).append(',')
                        .append(escapeCsv(v.getFixedPhrase())).append(',')
                        .append(escapeCsv(v.getRelatedWords())).append(',')
                        .append(escapeCsv(v.getNotes()))
                        .append('\n');
            }
            return csv.toString().getBytes(StandardCharsets.UTF_8);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể export CSV lúc này", e);
        }
    }

    private List<Vocabulary> parseCsv(MultipartFile file, VocabularySet set) throws Exception {
        List<Vocabulary> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] fields = line.split(",", -1);
                if (fields.length < 3) {
                    continue;
                }
                String word = safeGet(fields, 0);
                String meaning = safeGet(fields, 2);
                if (word.isBlank() || meaning.isBlank()) {
                    continue;
                }
                Vocabulary vocab = new Vocabulary();
                vocab.setVocabularySet(set);
                vocab.setWord(word);
                vocab.setPronunciation(safeGet(fields, 1));
                vocab.setMeaning(meaning);
                vocab.setDescription(safeGet(fields, 3));
                vocab.setExampleSentence(safeGet(fields, 4));
                vocab.setFixedPhrase(safeGet(fields, 5));
                vocab.setRelatedWords(safeGet(fields, 6));
                vocab.setNotes(safeGet(fields, 7));
                result.add(vocab);
            }
        }
        return result;
    }

    private List<Vocabulary> parseExcel(MultipartFile file, VocabularySet set) throws Exception {
        List<Vocabulary> result = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String word = formatter.formatCellValue(row.getCell(0)).trim();
                String meaning = formatter.formatCellValue(row.getCell(2)).trim();
                if (word.isBlank() || meaning.isBlank()) {
                    continue;
                }

                Vocabulary vocab = new Vocabulary();
                vocab.setVocabularySet(set);
                vocab.setWord(word);
                vocab.setPronunciation(formatter.formatCellValue(row.getCell(1)).trim());
                vocab.setMeaning(meaning);
                vocab.setDescription(formatter.formatCellValue(row.getCell(3)).trim());
                vocab.setExampleSentence(formatter.formatCellValue(row.getCell(4)).trim());
                vocab.setFixedPhrase(formatter.formatCellValue(row.getCell(5)).trim());
                vocab.setRelatedWords(formatter.formatCellValue(row.getCell(6)).trim());
                vocab.setNotes(formatter.formatCellValue(row.getCell(7)).trim());
                result.add(vocab);
            }
        }
        return result;
    }

    private String safeGet(String[] fields, int idx) {
        if (idx < 0 || idx >= fields.length) {
            return "";
        }
        return fields[idx] == null ? "" : fields[idx].trim();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
