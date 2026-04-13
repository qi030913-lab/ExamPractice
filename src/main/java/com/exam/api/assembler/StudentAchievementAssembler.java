package com.exam.api.assembler;

import com.exam.api.service.StudentAchievementService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StudentAchievementAssembler {

    public Map<String, Object> toPayload(StudentAchievementService.StudentAchievementSnapshot snapshot) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("summary", toSummary(snapshot.getSummary()));
        payload.put("scoreTrend", snapshot.getScoreTrend().stream().map(this::toScoreTrendItem).collect(Collectors.toList()));
        payload.put("questionTypeAccuracy", snapshot.getQuestionTypeAccuracy().stream().map(this::toTypeAccuracyItem).collect(Collectors.toList()));
        payload.put("subjectPerformance", snapshot.getSubjectPerformance().stream().map(this::toSubjectPerformanceItem).collect(Collectors.toList()));
        payload.put("latestUpdatedAt", snapshot.getLatestUpdatedAt());
        return payload;
    }

    private Map<String, Object> toSummary(StudentAchievementService.AchievementSummary summary) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("totalExamCount", summary.getTotalExamCount());
        item.put("completedExamCount", summary.getCompletedExamCount());
        item.put("inProgressCount", summary.getInProgressCount());
        item.put("averageScore", summary.getAverageScore());
        item.put("bestScore", summary.getBestScore());
        item.put("passCount", summary.getPassCount());
        item.put("failCount", summary.getFailCount());
        item.put("totalQuestionCount", summary.getTotalQuestionCount());
        item.put("totalAnsweredCount", summary.getTotalAnsweredCount());
        item.put("totalCorrectCount", summary.getTotalCorrectCount());
        item.put("accuracyRate", summary.getAccuracyRate());
        return item;
    }

    private Map<String, Object> toScoreTrendItem(StudentAchievementService.ScoreTrendItem scoreTrendItem) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", scoreTrendItem.getRecordId());
        item.put("paperId", scoreTrendItem.getPaperId());
        item.put("paperName", scoreTrendItem.getPaperName());
        item.put("subject", scoreTrendItem.getSubject());
        item.put("score", scoreTrendItem.getScore());
        item.put("totalScore", scoreTrendItem.getTotalScore());
        item.put("passScore", scoreTrendItem.getPassScore());
        item.put("status", scoreTrendItem.getStatus());
        item.put("submitTime", scoreTrendItem.getSubmitTime());
        item.put("startTime", scoreTrendItem.getStartTime());
        item.put("passed", scoreTrendItem.isPassed());
        return item;
    }

    private Map<String, Object> toTypeAccuracyItem(StudentAchievementService.TypeAccuracyItem typeAccuracyItem) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("questionType", typeAccuracyItem.getQuestionType());
        item.put("label", typeAccuracyItem.getLabel());
        item.put("totalCount", typeAccuracyItem.getTotalCount());
        item.put("answeredCount", typeAccuracyItem.getAnsweredCount());
        item.put("correctCount", typeAccuracyItem.getCorrectCount());
        item.put("accuracyRate", typeAccuracyItem.getAccuracyRate());
        return item;
    }

    private Map<String, Object> toSubjectPerformanceItem(StudentAchievementService.SubjectPerformanceItem subjectPerformanceItem) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("subject", subjectPerformanceItem.getSubject());
        item.put("recordCount", subjectPerformanceItem.getRecordCount());
        item.put("averageScore", subjectPerformanceItem.getAverageScore());
        item.put("passCount", subjectPerformanceItem.getPassCount());
        item.put("failCount", subjectPerformanceItem.getFailCount());
        item.put("totalQuestionCount", subjectPerformanceItem.getTotalQuestionCount());
        item.put("answeredCount", subjectPerformanceItem.getAnsweredCount());
        item.put("correctCount", subjectPerformanceItem.getCorrectCount());
        item.put("accuracyRate", subjectPerformanceItem.getAccuracyRate());
        item.put("latestSubmitTime", subjectPerformanceItem.getLatestSubmitTime());
        return item;
    }
}
