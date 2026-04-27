package com.minlish.service;

import com.minlish.dto.StudyRatingRequest;
import com.minlish.entity.StudyHistory;
import com.minlish.entity.User;
import com.minlish.entity.Vocabulary;

import java.util.List;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:54
 * File      : StudyService
 */

/**
 * Service xử lý luồng học: chấm điểm SRS, lấy từ cần ôn và lịch ôn tiếp theo.
 */

public interface StudyService {

    void processStudyResult(User user, StudyRatingRequest request);

    List<Vocabulary> getTodayReviewWords(User user);

    List<Vocabulary> getTodayReviewWordsBySet(User user, Long setId);

    List<StudyHistory> getUpcomingReviews(User user, int days);
}