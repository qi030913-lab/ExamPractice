package com.exam.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class TeacherWorkspaceDtos {
    private TeacherWorkspaceDtos() {
    }

    public record PaperSummary(
            int paperCount,
            long publishedCount,
            long unpublishedCount
    ) {
    }

    public record StudentListSummary(
            int studentCount
    ) {
    }

    public record StudentRecordSummary(
            int recordCount,
            long submittedCount,
            double averageScore
    ) {
    }

    public record TeacherPaperItem(
            Integer paperId,
            String paperName,
            String subject,
            Integer totalScore,
            Integer duration,
            Integer passScore,
            int questionCount,
            boolean published,
            String description,
            LocalDateTime createTime,
            LocalDateTime updateTime
    ) {
    }

    public record TeacherStudentItem(
            Integer userId,
            String realName,
            String loginId,
            String email,
            String phone,
            String gender,
            String status,
            LocalDateTime createTime,
            LocalDateTime updateTime,
            int recordCount,
            long submittedCount,
            double averageScore
    ) {
    }

    public record TeacherStudentRecordItem(
            Integer recordId,
            Integer paperId,
            String paperName,
            String status,
            BigDecimal score,
            LocalDateTime startTime,
            LocalDateTime submitTime,
            long durationSeconds
    ) {
    }

    public record QuestionItem(
            Integer questionId,
            String questionType,
            String subject,
            String content,
            String correctAnswer,
            Integer score,
            String difficulty
    ) {
    }

    public record QuestionDetailItem(
            Integer questionId,
            String questionType,
            String subject,
            String content,
            String correctAnswer,
            Integer score,
            String difficulty,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String analysis
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

    public record TeacherStudentRecordDetailItem(
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
            boolean passed
    ) {
    }

    public record TeacherPapersPayload(
            AuthUserResponse user,
            PaperSummary summary,
            List<TeacherPaperItem> papers
    ) {
    }

    public record PaperMutationPayload(
            TeacherPaperItem paper
    ) {
    }

    public record DeletePaperPayload(
            Integer paperId
    ) {
    }

    public record TeacherPaperDetailPayload(
            TeacherPaperItem paper,
            List<QuestionDetailItem> questions
    ) {
    }

    public record TeacherStudentsPayload(
            AuthUserResponse user,
            StudentListSummary summary,
            List<TeacherStudentItem> students
    ) {
    }

    public record TeacherStudentRecordsPayload(
            AuthUserResponse student,
            StudentRecordSummary summary,
            List<TeacherStudentRecordItem> records
    ) {
    }

    public record TeacherStudentDetailPayload(
            TeacherStudentItem student,
            StudentRecordSummary summary,
            List<TeacherStudentRecordItem> records
    ) {
    }

    public record TeacherStudentRecordDetailPayload(
            TeacherStudentItem student,
            TeacherStudentRecordDetailItem record,
            List<AnswerRecordItem> answers
    ) {
    }

    public record ImportTemplatePayload(
            String fileName,
            String content
    ) {
    }

    public record ImportPaperSummary(
            int sourceQuestionCount,
            int linkedQuestionCount,
            int createdQuestionCount,
            int reusedQuestionCount
    ) {
    }

    public record ImportPaperPayload(
            TeacherPaperItem paper,
            List<QuestionItem> questions,
            ImportPaperSummary summary
    ) {
    }
}
