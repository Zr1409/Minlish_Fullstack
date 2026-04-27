package com.minlish.util;

import lombok.Getter;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:58
 * File      : SM2Util
 */

public class SM2Util {

    private SM2Util() {
        // Utility class: không khởi tạo đối tượng.
    }

    /**
     * Kết quả tính toán SM-2
     */
    @Getter
    public static class SM2Result {
        private final double easeFactor;
        private final int intervalDays;
        private final int repetitions;

        public SM2Result(double easeFactor, int intervalDays, int repetitions) {
            this.easeFactor = easeFactor;
            this.intervalDays = intervalDays;
            this.repetitions = repetitions;
        }
    }

    /**
      * Tính toán các thông số mới dựa trên rating.
      *
      * @param rating "repeat", "hard", "good", "easy"
      * @param oldEaseFactor Hệ số độ dễ cũ
     * @param oldInterval Khoảng cách cũ (ngày)
     * @param oldRepetitions Số lần lặp lại cũ
     * @return SM2Result chứa ease factor mới, interval mới, repetitions mới
     */
    public static final int MAX_INTERVAL_DAYS = 31;

    public static SM2Result calculate(String rating, double oldEaseFactor, int oldInterval, int oldRepetitions) {
        if (oldInterval < 1) {
            oldInterval = 1;
        }
        double newEaseFactor = oldEaseFactor;
        int newInterval;
        int newRepetitions = oldRepetitions;

        // Ở đây cố tình giữ mapping riêng của app để phù hợp với cách người dùng chấm điểm.
        switch (rating) {
            case "repeat":
            case "again":
                newRepetitions = oldRepetitions + 3;
                newInterval = 1;
                newEaseFactor = Math.max(1.3, oldEaseFactor - 0.2);
                break;
            case "hard":
                newInterval = (int) Math.ceil(oldInterval * 0.7);
                newEaseFactor = Math.max(1.3, oldEaseFactor - 0.15);
                newRepetitions = oldRepetitions + 2;
                break;
            case "good":
                newInterval = (int) Math.ceil(oldInterval * oldEaseFactor);
                newEaseFactor = oldEaseFactor + 0.1;
                newRepetitions = oldRepetitions + 1;
                break;
            case "easy":
                newInterval = (int) Math.ceil(oldInterval * oldEaseFactor * 1.3);
                newEaseFactor = oldEaseFactor + 0.2;
                newRepetitions = oldRepetitions + 1;
                break;
            default:
                throw new IllegalArgumentException("Rating không hợp lệ: " + rating);
        }

        newInterval = clampInt(newInterval, 1, MAX_INTERVAL_DAYS);
        newEaseFactor = clampDouble(newEaseFactor, 1.3, 2.5);

        return new SM2Result(newEaseFactor, newInterval, newRepetitions);
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
