package com.exam.api.controller;

import com.exam.api.common.ApiResponse;
import com.exam.api.dto.AuthUserResponse;
import com.exam.exception.BusinessException;
import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.User;
import com.exam.model.enums.ExamStatus;
import com.exam.model.enums.QuestionType;
import com.exam.model.enums.UserRole;
import com.exam.service.ExamService;
import com.exam.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
public class StudentAchievementController {
    private final UserService userService;
    private final ExamService examService;

    public StudentAchievementController(UserService userService, ExamService examService) {
        this.userService = userService;
        this.examService = examService;
    }

    @GetMapping("/{userId}/achievement")
    public ApiResponse<Map<String, Object>> getStudentAchievement(@PathVariable("userId") Integer userId) {
        User student = requireStudent(userId);
        List<ExamRecord> allRecords = examService.getStudentExamRecordsOptimized(userId);
        List<ExamRecord> completedRecords = allRecords.stream()
                .filter(this::isCompletedRecord)
                .sorted(buildRecordTimeComparator())
                .collect(Collectors.toList());

        List<Integer> completedRecordIds = completedRecords.stream()
                .map(ExamRecord::getRecordId)
                .filter(recordId -> recordId != null)
                .collect(Collectors.toList());
        Map<Integer, List<AnswerRecord>> answerRecordsMap = examService.getAnswerRecordsBatch(completedRecordIds);

        Map<String, SubjectAggregate> subjectAggregates = new LinkedHashMap<>();
        Map<String, TypeAggregate> typeAggregates = new LinkedHashMap<>();

        long totalQuestionCount = 0;
        long totalAnsweredCount = 0;
        long totalCorrectCount = 0;
        int passCount = 0;
        double totalScore = 0;
        int scoredCount = 0;
        BigDecimal bestScore = null;

        for (ExamRecord record : completedRecords) {
            Paper paper = record.getPaper();
            String subject = normalizeCategory(paper != null ? paper.getSubject() : null, "未分类科目");
            SubjectAggregate subjectAggregate = subjectAggregates.computeIfAbsent(subject, key -> new SubjectAggregate(subject));
            subjectAggregate.recordCount++;
            subjectAggregate.latestSubmitTime = resolveRecordTime(record);

            if (record.getScore() != null) {
                double numericScore = record.getScore().doubleValue();
                totalScore += numericScore;
                scoredCount++;
                subjectAggregate.scoreSum += numericScore;
                subjectAggregate.scoredCount++;
                if (bestScore == null || record.getScore().compareTo(bestScore) > 0) {
                    bestScore = record.getScore();
                }
            }

            boolean passed = isPassed(record, paper);
            if (passed) {
                passCount++;
                subjectAggregate.passCount++;
            } else {
                subjectAggregate.failCount++;
            }

            List<AnswerRecord> answerRecords = answerRecordsMap.getOrDefault(record.getRecordId(), Collections.emptyList());
            for (AnswerRecord answerRecord : answerRecords) {
                totalQuestionCount++;
                subjectAggregate.totalQuestionCount++;

                if (isAnswered(answerRecord)) {
                    totalAnsweredCount++;
                    subjectAggregate.answeredCount++;
                }

                if (Boolean.TRUE.equals(answerRecord.getIsCorrect())) {
                    totalCorrectCount++;
                    subjectAggregate.correctCount++;
                }

                Question question = answerRecord.getQuestion();
                QuestionType questionType = question == null ? null : question.getQuestionType();
                String typeKey = questionType == null ? "OTHER" : questionType.name();
                String typeLabel = questionType == null ? "其他题型" : questionType.getDescription();

                TypeAggregate typeAggregate = typeAggregates.computeIfAbsent(typeKey, key -> new TypeAggregate(typeKey, typeLabel));
                typeAggregate.totalCount++;
                if (isAnswered(answerRecord)) {
                    typeAggregate.answeredCount++;
                }
                if (Boolean.TRUE.equals(answerRecord.getIsCorrect())) {
                    typeAggregate.correctCount++;
                }
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalExamCount", allRecords.size());
        summary.put("completedExamCount", completedRecords.size());
        summary.put("inProgressCount", allRecords.stream().filter(record -> record.getStatus() == ExamStatus.IN_PROGRESS).count());
        summary.put("averageScore", roundDouble(scoredCount == 0 ? 0 : totalScore / scoredCount));
        summary.put("bestScore", bestScore);
        summary.put("passCount", passCount);
        summary.put("failCount", Math.max(0, completedRecords.size() - passCount));
        summary.put("totalQuestionCount", totalQuestionCount);
        summary.put("totalAnsweredCount", totalAnsweredCount);
        summary.put("totalCorrectCount", totalCorrectCount);
        summary.put("accuracyRate", roundDouble(totalQuestionCount == 0 ? 0 : totalCorrectCount * 100.0 / totalQuestionCount));

        List<Map<String, Object>> scoreTrend = completedRecords.stream()
                .map(this::toScoreTrendItem)
                .collect(Collectors.toList());

        List<Map<String, Object>> questionTypeAccuracy = typeAggregates.values().stream()
                .sorted(Comparator.comparingLong((TypeAggregate item) -> item.totalCount).reversed())
                .map(this::toTypeAccuracyItem)
                .collect(Collectors.toList());

        List<Map<String, Object>> subjectPerformance = subjectAggregates.values().stream()
                .sorted(Comparator
                        .comparingInt((SubjectAggregate item) -> item.recordCount).reversed()
                        .thenComparing(item -> item.subject))
                .map(this::toSubjectPerformanceItem)
                .collect(Collectors.toList());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user", AuthUserResponse.from(student));
        payload.put("summary", summary);
        payload.put("scoreTrend", scoreTrend);
        payload.put("questionTypeAccuracy", questionTypeAccuracy);
        payload.put("subjectPerformance", subjectPerformance);
        payload.put("latestUpdatedAt", LocalDateTime.now());
        return ApiResponse.success("学生成就数据加载成功", payload);
    }

    private User requireStudent(Integer userId) {
        User user = userService.getUserById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException("当前用户不是学生角色");
        }
        return user;
    }

    private boolean isCompletedRecord(ExamRecord record) {
        return record.getStatus() == ExamStatus.SUBMITTED || record.getStatus() == ExamStatus.TIMEOUT;
    }

    private Comparator<ExamRecord> buildRecordTimeComparator() {
        return Comparator
                .comparing(this::resolveRecordTime, Comparator.nullsLast(LocalDateTime::compareTo))
                .thenComparing(ExamRecord::getRecordId, Comparator.nullsLast(Integer::compareTo));
    }

    private LocalDateTime resolveRecordTime(ExamRecord record) {
        if (record.getSubmitTime() != null) {
            return record.getSubmitTime();
        }
        if (record.getEndTime() != null) {
            return record.getEndTime();
        }
        return record.getStartTime();
    }

    private boolean isPassed(ExamRecord record, Paper paper) {
        return record.getScore() != null
                && paper != null
                && paper.getPassScore() != null
                && record.getScore().compareTo(BigDecimal.valueOf(paper.getPassScore())) >= 0;
    }

    private boolean isAnswered(AnswerRecord answerRecord) {
        return answerRecord.getStudentAnswer() != null && !answerRecord.getStudentAnswer().trim().isEmpty();
    }

    private String normalizeCategory(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private double roundDouble(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Map<String, Object> toScoreTrendItem(ExamRecord record) {
        Paper paper = record.getPaper();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", record.getRecordId());
        item.put("paperId", record.getPaperId());
        item.put("paperName", paper != null ? paper.getPaperName() : null);
        item.put("subject", paper != null ? paper.getSubject() : null);
        item.put("score", record.getScore());
        item.put("totalScore", paper != null ? paper.getTotalScore() : null);
        item.put("passScore", paper != null ? paper.getPassScore() : null);
        item.put("status", record.getStatus() == null ? null : record.getStatus().name());
        item.put("submitTime", record.getSubmitTime());
        item.put("startTime", record.getStartTime());
        item.put("passed", isPassed(record, paper));
        return item;
    }

    private Map<String, Object> toTypeAccuracyItem(TypeAggregate typeAggregate) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("questionType", typeAggregate.type);
        item.put("label", typeAggregate.label);
        item.put("totalCount", typeAggregate.totalCount);
        item.put("answeredCount", typeAggregate.answeredCount);
        item.put("correctCount", typeAggregate.correctCount);
        item.put("accuracyRate", roundDouble(typeAggregate.totalCount == 0 ? 0 : typeAggregate.correctCount * 100.0 / typeAggregate.totalCount));
        return item;
    }

    private Map<String, Object> toSubjectPerformanceItem(SubjectAggregate subjectAggregate) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("subject", subjectAggregate.subject);
        item.put("recordCount", subjectAggregate.recordCount);
        item.put("averageScore", roundDouble(subjectAggregate.scoredCount == 0 ? 0 : subjectAggregate.scoreSum / subjectAggregate.scoredCount));
        item.put("passCount", subjectAggregate.passCount);
        item.put("failCount", subjectAggregate.failCount);
        item.put("totalQuestionCount", subjectAggregate.totalQuestionCount);
        item.put("answeredCount", subjectAggregate.answeredCount);
        item.put("correctCount", subjectAggregate.correctCount);
        item.put("accuracyRate", roundDouble(subjectAggregate.totalQuestionCount == 0 ? 0 : subjectAggregate.correctCount * 100.0 / subjectAggregate.totalQuestionCount));
        item.put("latestSubmitTime", subjectAggregate.latestSubmitTime);
        return item;
    }

    private static class SubjectAggregate {
        private final String subject;
        private int recordCount;
        private double scoreSum;
        private int scoredCount;
        private int passCount;
        private int failCount;
        private long totalQuestionCount;
        private long answeredCount;
        private long correctCount;
        private LocalDateTime latestSubmitTime;

        private SubjectAggregate(String subject) {
            this.subject = subject;
        }
    }

    private static class TypeAggregate {
        private final String type;
        private final String label;
        private long totalCount;
        private long answeredCount;
        private long correctCount;

        private TypeAggregate(String type, String label) {
            this.type = type;
            this.label = label;
        }
    }
}
