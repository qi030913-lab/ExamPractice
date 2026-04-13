package com.exam.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class StudentWorkspaceDtos {
    private StudentWorkspaceDtos() {
    }

    public record PaperSummary(
            int paperCount,
            long completedCount,
            long inProgressCount
    ) {
    }

    public record RecordSummary(
            int recordCount,
            long submittedCount,
            double averageScore
    ) {
    }

    public record StudentPaperItem(
            Integer paperId,
            String paperName,
            String subject,
            Integer duration,
            Integer totalScore,
            Integer passScore,
            String description,
            int questionCount,
            boolean published,
            boolean hasInProgressRecord,
            StudentRecordCard latestRecord
    ) {
    }

    public record QuestionExamItem(
            Integer questionId,
            String questionType,
            String subject,
            String content,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            Integer score
    ) {
    }

    public record ExamLifecycleRecordItem(
            Integer recordId,
            Integer studentId,
            Integer paperId,
            String status,
            LocalDateTime startTime,
            LocalDateTime submitTime,
            long durationSeconds,
            boolean resumeAvailable
    ) {
    }

    public record StudentRecordCard(
            Integer recordId,
            Integer paperId,
            String paperName,
            String status,
            BigDecimal score,
            LocalDateTime submitTime,
            LocalDateTime startTime,
            long durationSeconds,
            boolean resumeAvailable
    ) {
    }

    public record StudentScoreRecordItem(
            Integer recordId,
            Integer paperId,
            String paperName,
            Integer totalScore,
            BigDecimal score,
            String status,
            LocalDateTime submitTime,
            LocalDateTime startTime,
            long durationSeconds,
            long correctCount,
            long wrongCount,
            boolean resumeAvailable
    ) {
    }

    public record StudentRecordDetailItem(
            Integer recordId,
            Integer paperId,
            String paperName,
            String subject,
            Integer totalScore,
            Integer passScore,
            BigDecimal score,
            String status,
            LocalDateTime startTime,
            LocalDateTime submitTime,
            long durationSeconds,
            int questionCount,
            long answeredCount,
            long correctCount,
            long wrongCount,
            boolean resumeAvailable,
            boolean passed
    ) {
    }

    public record AnswerRecordItem(
            Integer answerId,
            Integer recordId,
            Integer questionId,
            String questionType,
            String content,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String studentAnswer,
            String correctAnswer,
            String analysis,
            BigDecimal score,
            boolean isCorrect
    ) {
    }

    public record SubmitResultItem(
            Integer recordId,
            Integer paperId,
            String paperName,
            String subject,
            BigDecimal score,
            Integer totalScore,
            Integer passScore,
            boolean passed,
            String status,
            LocalDateTime submitTime,
            long durationSeconds,
            int questionCount,
            long answeredCount,
            long correctCount,
            long wrongCount
    ) {
    }

    public record StudentPapersPayload(
            AuthUserResponse user,
            PaperSummary summary,
            List<StudentPaperItem> papers
    ) {
    }

    public record StudentPaperDetailPayload(
            AuthUserResponse user,
            StudentPaperItem paper,
            List<QuestionExamItem> questions
    ) {
    }

    public record StartExamPayload(
            AuthUserResponse user,
            ExamLifecycleRecordItem record,
            StudentPaperItem paper,
            List<QuestionExamItem> questions,
            long remainingSeconds,
            LocalDateTime deadlineTime,
            boolean resumed
    ) {
    }

    public record StudentRecordsPayload(
            AuthUserResponse user,
            RecordSummary summary,
            List<StudentScoreRecordItem> records
    ) {
    }

    public record StudentRecordDetailPayload(
            AuthUserResponse user,
            StudentRecordDetailItem record,
            List<AnswerRecordItem> answers
    ) {
    }

    public record StudentExamSessionPayload(
            AuthUserResponse user,
            ExamLifecycleRecordItem record,
            StudentPaperItem paper,
            List<QuestionExamItem> questions,
            long remainingSeconds,
            LocalDateTime deadlineTime
    ) {
    }

    public record StudentSubmitResultPayload(
            AuthUserResponse user,
            SubmitResultItem result
    ) {
    }
}
