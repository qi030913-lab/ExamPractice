package com.exam.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class StudentAchievementDtos {
    private StudentAchievementDtos() {
    }

    public record AchievementSummary(
            int totalExamCount,
            int completedExamCount,
            long inProgressCount,
            double averageScore,
            BigDecimal bestScore,
            int passCount,
            int failCount,
            long totalQuestionCount,
            long totalAnsweredCount,
            long totalCorrectCount,
            double accuracyRate
    ) {
    }

    public record ScoreTrendItem(
            Integer recordId,
            Integer paperId,
            String paperName,
            String subject,
            BigDecimal score,
            Integer totalScore,
            Integer passScore,
            String status,
            LocalDateTime submitTime,
            LocalDateTime startTime,
            boolean passed
    ) {
    }

    public record TypeAccuracyItem(
            String questionType,
            String label,
            long totalCount,
            long answeredCount,
            long correctCount,
            double accuracyRate
    ) {
    }

    public record SubjectPerformanceItem(
            String subject,
            int recordCount,
            double averageScore,
            int passCount,
            int failCount,
            long totalQuestionCount,
            long answeredCount,
            long correctCount,
            double accuracyRate,
            LocalDateTime latestSubmitTime
    ) {
    }

    public record StudentAchievementPayload(
            AuthUserResponse user,
            AchievementSummary summary,
            List<ScoreTrendItem> scoreTrend,
            List<TypeAccuracyItem> questionTypeAccuracy,
            List<SubjectPerformanceItem> subjectPerformance,
            LocalDateTime latestUpdatedAt
    ) {
    }
}
