package com.exam.api.service;

import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.enums.ExamStatus;
import com.exam.model.enums.QuestionType;
import com.exam.service.ExamService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudentAchievementService {
    private static final String OTHER_TYPE_KEY = "OTHER";
    private static final String OTHER_TYPE_LABEL = "其他题型";
    private static final String UNKNOWN_SUBJECT = "未分类科目";

    private final ExamService examService;

    public StudentAchievementService(ExamService examService) {
        this.examService = examService;
    }

    public StudentAchievementSnapshot buildSnapshot(Integer userId) {
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

        Map<String, SubjectAccumulator> subjectAccumulators = new LinkedHashMap<>();
        Map<String, TypeAccumulator> typeAccumulators = new LinkedHashMap<>();

        long totalQuestionCount = 0;
        long totalAnsweredCount = 0;
        long totalCorrectCount = 0;
        int passCount = 0;
        double totalScore = 0;
        int scoredCount = 0;
        BigDecimal bestScore = null;

        for (ExamRecord record : completedRecords) {
            Paper paper = record.getPaper();
            String subject = normalizeCategory(paper != null ? paper.getSubject() : null, UNKNOWN_SUBJECT);
            SubjectAccumulator subjectAccumulator = subjectAccumulators.computeIfAbsent(subject, SubjectAccumulator::new);
            subjectAccumulator.recordCount++;
            subjectAccumulator.latestSubmitTime = resolveRecordTime(record);

            if (record.getScore() != null) {
                double numericScore = record.getScore().doubleValue();
                totalScore += numericScore;
                scoredCount++;
                subjectAccumulator.scoreSum += numericScore;
                subjectAccumulator.scoredCount++;
                if (bestScore == null || record.getScore().compareTo(bestScore) > 0) {
                    bestScore = record.getScore();
                }
            }

            boolean passed = isPassed(record, paper);
            if (passed) {
                passCount++;
                subjectAccumulator.passCount++;
            } else {
                subjectAccumulator.failCount++;
            }

            List<AnswerRecord> answerRecords = answerRecordsMap.getOrDefault(record.getRecordId(), Collections.emptyList());
            for (AnswerRecord answerRecord : answerRecords) {
                totalQuestionCount++;
                subjectAccumulator.totalQuestionCount++;

                if (isAnswered(answerRecord)) {
                    totalAnsweredCount++;
                    subjectAccumulator.answeredCount++;
                }

                if (Boolean.TRUE.equals(answerRecord.getIsCorrect())) {
                    totalCorrectCount++;
                    subjectAccumulator.correctCount++;
                }

                Question question = answerRecord.getQuestion();
                QuestionType questionType = question == null ? null : question.getQuestionType();
                String typeKey = questionType == null ? OTHER_TYPE_KEY : questionType.name();
                String typeLabel = questionType == null ? OTHER_TYPE_LABEL : questionType.getDescription();

                TypeAccumulator typeAccumulator = typeAccumulators.computeIfAbsent(typeKey, key -> new TypeAccumulator(typeKey, typeLabel));
                typeAccumulator.totalCount++;
                if (isAnswered(answerRecord)) {
                    typeAccumulator.answeredCount++;
                }
                if (Boolean.TRUE.equals(answerRecord.getIsCorrect())) {
                    typeAccumulator.correctCount++;
                }
            }
        }

        AchievementSummary summary = new AchievementSummary(
                allRecords.size(),
                completedRecords.size(),
                allRecords.stream().filter(record -> record.getStatus() == ExamStatus.IN_PROGRESS).count(),
                roundDouble(scoredCount == 0 ? 0 : totalScore / scoredCount),
                bestScore,
                passCount,
                Math.max(0, completedRecords.size() - passCount),
                totalQuestionCount,
                totalAnsweredCount,
                totalCorrectCount,
                roundDouble(totalQuestionCount == 0 ? 0 : totalCorrectCount * 100.0 / totalQuestionCount)
        );

        List<ScoreTrendItem> scoreTrend = completedRecords.stream()
                .map(this::toScoreTrendItem)
                .collect(Collectors.toList());

        List<TypeAccuracyItem> questionTypeAccuracy = typeAccumulators.values().stream()
                .sorted(Comparator.comparingLong((TypeAccumulator item) -> item.totalCount).reversed())
                .map(this::toTypeAccuracyItem)
                .collect(Collectors.toList());

        List<SubjectPerformanceItem> subjectPerformance = subjectAccumulators.values().stream()
                .sorted(Comparator
                        .comparingInt((SubjectAccumulator item) -> item.recordCount).reversed()
                        .thenComparing(item -> item.subject))
                .map(this::toSubjectPerformanceItem)
                .collect(Collectors.toList());

        return new StudentAchievementSnapshot(summary, scoreTrend, questionTypeAccuracy, subjectPerformance, LocalDateTime.now());
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

    private ScoreTrendItem toScoreTrendItem(ExamRecord record) {
        Paper paper = record.getPaper();
        return new ScoreTrendItem(
                record.getRecordId(),
                record.getPaperId(),
                paper != null ? paper.getPaperName() : null,
                paper != null ? paper.getSubject() : null,
                record.getScore(),
                paper != null ? paper.getTotalScore() : null,
                paper != null ? paper.getPassScore() : null,
                record.getStatus() == null ? null : record.getStatus().name(),
                record.getSubmitTime(),
                record.getStartTime(),
                isPassed(record, paper)
        );
    }

    private TypeAccuracyItem toTypeAccuracyItem(TypeAccumulator typeAccumulator) {
        return new TypeAccuracyItem(
                typeAccumulator.type,
                typeAccumulator.label,
                typeAccumulator.totalCount,
                typeAccumulator.answeredCount,
                typeAccumulator.correctCount,
                roundDouble(typeAccumulator.totalCount == 0 ? 0 : typeAccumulator.correctCount * 100.0 / typeAccumulator.totalCount)
        );
    }

    private SubjectPerformanceItem toSubjectPerformanceItem(SubjectAccumulator subjectAccumulator) {
        return new SubjectPerformanceItem(
                subjectAccumulator.subject,
                subjectAccumulator.recordCount,
                roundDouble(subjectAccumulator.scoredCount == 0 ? 0 : subjectAccumulator.scoreSum / subjectAccumulator.scoredCount),
                subjectAccumulator.passCount,
                subjectAccumulator.failCount,
                subjectAccumulator.totalQuestionCount,
                subjectAccumulator.answeredCount,
                subjectAccumulator.correctCount,
                roundDouble(subjectAccumulator.totalQuestionCount == 0 ? 0 : subjectAccumulator.correctCount * 100.0 / subjectAccumulator.totalQuestionCount),
                subjectAccumulator.latestSubmitTime
        );
    }

    public static class StudentAchievementSnapshot {
        private final AchievementSummary summary;
        private final List<ScoreTrendItem> scoreTrend;
        private final List<TypeAccuracyItem> questionTypeAccuracy;
        private final List<SubjectPerformanceItem> subjectPerformance;
        private final LocalDateTime latestUpdatedAt;

        public StudentAchievementSnapshot(
                AchievementSummary summary,
                List<ScoreTrendItem> scoreTrend,
                List<TypeAccuracyItem> questionTypeAccuracy,
                List<SubjectPerformanceItem> subjectPerformance,
                LocalDateTime latestUpdatedAt
        ) {
            this.summary = summary;
            this.scoreTrend = scoreTrend;
            this.questionTypeAccuracy = questionTypeAccuracy;
            this.subjectPerformance = subjectPerformance;
            this.latestUpdatedAt = latestUpdatedAt;
        }

        public AchievementSummary getSummary() {
            return summary;
        }

        public List<ScoreTrendItem> getScoreTrend() {
            return scoreTrend;
        }

        public List<TypeAccuracyItem> getQuestionTypeAccuracy() {
            return questionTypeAccuracy;
        }

        public List<SubjectPerformanceItem> getSubjectPerformance() {
            return subjectPerformance;
        }

        public LocalDateTime getLatestUpdatedAt() {
            return latestUpdatedAt;
        }
    }

    public static class AchievementSummary {
        private final int totalExamCount;
        private final int completedExamCount;
        private final long inProgressCount;
        private final double averageScore;
        private final BigDecimal bestScore;
        private final int passCount;
        private final int failCount;
        private final long totalQuestionCount;
        private final long totalAnsweredCount;
        private final long totalCorrectCount;
        private final double accuracyRate;

        public AchievementSummary(
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
            this.totalExamCount = totalExamCount;
            this.completedExamCount = completedExamCount;
            this.inProgressCount = inProgressCount;
            this.averageScore = averageScore;
            this.bestScore = bestScore;
            this.passCount = passCount;
            this.failCount = failCount;
            this.totalQuestionCount = totalQuestionCount;
            this.totalAnsweredCount = totalAnsweredCount;
            this.totalCorrectCount = totalCorrectCount;
            this.accuracyRate = accuracyRate;
        }

        public int getTotalExamCount() {
            return totalExamCount;
        }

        public int getCompletedExamCount() {
            return completedExamCount;
        }

        public long getInProgressCount() {
            return inProgressCount;
        }

        public double getAverageScore() {
            return averageScore;
        }

        public BigDecimal getBestScore() {
            return bestScore;
        }

        public int getPassCount() {
            return passCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public long getTotalQuestionCount() {
            return totalQuestionCount;
        }

        public long getTotalAnsweredCount() {
            return totalAnsweredCount;
        }

        public long getTotalCorrectCount() {
            return totalCorrectCount;
        }

        public double getAccuracyRate() {
            return accuracyRate;
        }
    }

    public static class ScoreTrendItem {
        private final Integer recordId;
        private final Integer paperId;
        private final String paperName;
        private final String subject;
        private final BigDecimal score;
        private final Integer totalScore;
        private final Integer passScore;
        private final String status;
        private final LocalDateTime submitTime;
        private final LocalDateTime startTime;
        private final boolean passed;

        public ScoreTrendItem(
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
            this.recordId = recordId;
            this.paperId = paperId;
            this.paperName = paperName;
            this.subject = subject;
            this.score = score;
            this.totalScore = totalScore;
            this.passScore = passScore;
            this.status = status;
            this.submitTime = submitTime;
            this.startTime = startTime;
            this.passed = passed;
        }

        public Integer getRecordId() {
            return recordId;
        }

        public Integer getPaperId() {
            return paperId;
        }

        public String getPaperName() {
            return paperName;
        }

        public String getSubject() {
            return subject;
        }

        public BigDecimal getScore() {
            return score;
        }

        public Integer getTotalScore() {
            return totalScore;
        }

        public Integer getPassScore() {
            return passScore;
        }

        public String getStatus() {
            return status;
        }

        public LocalDateTime getSubmitTime() {
            return submitTime;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public boolean isPassed() {
            return passed;
        }
    }

    public static class TypeAccuracyItem {
        private final String questionType;
        private final String label;
        private final long totalCount;
        private final long answeredCount;
        private final long correctCount;
        private final double accuracyRate;

        public TypeAccuracyItem(
                String questionType,
                String label,
                long totalCount,
                long answeredCount,
                long correctCount,
                double accuracyRate
        ) {
            this.questionType = questionType;
            this.label = label;
            this.totalCount = totalCount;
            this.answeredCount = answeredCount;
            this.correctCount = correctCount;
            this.accuracyRate = accuracyRate;
        }

        public String getQuestionType() {
            return questionType;
        }

        public String getLabel() {
            return label;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public long getAnsweredCount() {
            return answeredCount;
        }

        public long getCorrectCount() {
            return correctCount;
        }

        public double getAccuracyRate() {
            return accuracyRate;
        }
    }

    public static class SubjectPerformanceItem {
        private final String subject;
        private final int recordCount;
        private final double averageScore;
        private final int passCount;
        private final int failCount;
        private final long totalQuestionCount;
        private final long answeredCount;
        private final long correctCount;
        private final double accuracyRate;
        private final LocalDateTime latestSubmitTime;

        public SubjectPerformanceItem(
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
            this.subject = subject;
            this.recordCount = recordCount;
            this.averageScore = averageScore;
            this.passCount = passCount;
            this.failCount = failCount;
            this.totalQuestionCount = totalQuestionCount;
            this.answeredCount = answeredCount;
            this.correctCount = correctCount;
            this.accuracyRate = accuracyRate;
            this.latestSubmitTime = latestSubmitTime;
        }

        public String getSubject() {
            return subject;
        }

        public int getRecordCount() {
            return recordCount;
        }

        public double getAverageScore() {
            return averageScore;
        }

        public int getPassCount() {
            return passCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public long getTotalQuestionCount() {
            return totalQuestionCount;
        }

        public long getAnsweredCount() {
            return answeredCount;
        }

        public long getCorrectCount() {
            return correctCount;
        }

        public double getAccuracyRate() {
            return accuracyRate;
        }

        public LocalDateTime getLatestSubmitTime() {
            return latestSubmitTime;
        }
    }

    private static class SubjectAccumulator {
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

        private SubjectAccumulator(String subject) {
            this.subject = subject;
        }
    }

    private static class TypeAccumulator {
        private final String type;
        private final String label;
        private long totalCount;
        private long answeredCount;
        private long correctCount;

        private TypeAccumulator(String type, String label) {
            this.type = type;
            this.label = label;
        }
    }
}
