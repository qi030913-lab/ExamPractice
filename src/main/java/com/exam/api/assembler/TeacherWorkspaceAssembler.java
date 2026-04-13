package com.exam.api.assembler;

import com.exam.api.dto.TeacherWorkspaceDtos;
import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TeacherWorkspaceAssembler {
    private final ExamRecordStatisticsAssembler statisticsAssembler;

    public TeacherWorkspaceAssembler(ExamRecordStatisticsAssembler statisticsAssembler) {
        this.statisticsAssembler = statisticsAssembler;
    }

    public TeacherWorkspaceDtos.TeacherPaperItem toTeacherPaperItem(Paper paper) {
        return new TeacherWorkspaceDtos.TeacherPaperItem(
                paper.getPaperId(),
                paper.getPaperName(),
                paper.getSubject(),
                paper.getTotalScore(),
                paper.getDuration(),
                paper.getPassScore(),
                resolveQuestionCount(paper),
                Boolean.TRUE.equals(paper.getIsPublished()),
                paper.getDescription(),
                paper.getCreateTime(),
                paper.getUpdateTime()
        );
    }

    public TeacherWorkspaceDtos.TeacherStudentItem toTeacherStudentItem(User student, List<ExamRecord> records) {
        TeacherWorkspaceDtos.StudentRecordSummary summary = buildStudentSummary(records);
        return new TeacherWorkspaceDtos.TeacherStudentItem(
                student.getUserId(),
                student.getRealName(),
                student.getLoginId(),
                student.getEmail(),
                student.getPhone(),
                student.getGender(),
                student.getStatus(),
                student.getCreateTime(),
                student.getUpdateTime(),
                summary.recordCount(),
                summary.submittedCount(),
                summary.averageScore()
        );
    }

    public TeacherWorkspaceDtos.TeacherStudentRecordItem toTeacherStudentRecordItem(ExamRecord record) {
        return new TeacherWorkspaceDtos.TeacherStudentRecordItem(
                record.getRecordId(),
                record.getPaperId(),
                record.getPaper() != null ? record.getPaper().getPaperName() : null,
                record.getStatus() == null ? null : record.getStatus().name(),
                record.getScore(),
                record.getStartTime(),
                record.getSubmitTime(),
                calculateDurationSeconds(record)
        );
    }

    public TeacherWorkspaceDtos.QuestionItem toQuestionItem(Question question) {
        return new TeacherWorkspaceDtos.QuestionItem(
                question.getQuestionId(),
                question.getQuestionType() == null ? null : question.getQuestionType().name(),
                question.getSubject(),
                question.getContent(),
                question.getCorrectAnswer(),
                question.getScore(),
                question.getDifficulty() == null ? null : question.getDifficulty().name()
        );
    }

    public TeacherWorkspaceDtos.QuestionDetailItem toQuestionDetailItem(Question question) {
        TeacherWorkspaceDtos.QuestionItem item = toQuestionItem(question);
        return new TeacherWorkspaceDtos.QuestionDetailItem(
                item.questionId(),
                item.questionType(),
                item.subject(),
                item.content(),
                item.correctAnswer(),
                item.score(),
                item.difficulty(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getAnalysis()
        );
    }

    public TeacherWorkspaceDtos.TeacherStudentRecordDetailItem toTeacherStudentRecordDetailItem(
            ExamRecord record,
            Paper paper,
            List<AnswerRecord> answerRecords
    ) {
        ExamRecordStatisticsAssembler.AnswerSummary answerSummary = statisticsAssembler.summarizeAnswers(answerRecords);
        return toTeacherStudentRecordDetailItem(
                record,
                paper,
                answerRecords,
                answerSummary.getAnsweredCount(),
                answerSummary.getCorrectCount(),
                answerSummary.getWrongCount()
        );
    }

    public TeacherWorkspaceDtos.TeacherStudentRecordDetailItem toTeacherStudentRecordDetailItem(
            ExamRecord record,
            Paper paper,
            List<AnswerRecord> answerRecords,
            long answeredCount,
            long correctCount,
            long wrongCount
    ) {
        return new TeacherWorkspaceDtos.TeacherStudentRecordDetailItem(
                record.getRecordId(),
                record.getPaperId(),
                paper != null ? paper.getPaperName() : null,
                paper != null ? paper.getSubject() : null,
                paper != null ? paper.getTotalScore() : null,
                paper != null ? paper.getPassScore() : null,
                record.getScore(),
                record.getStatus() == null ? null : record.getStatus().name(),
                record.getStartTime(),
                record.getSubmitTime(),
                calculateDurationSeconds(record),
                answerRecords.size(),
                answeredCount,
                correctCount,
                wrongCount,
                record.getScore() != null
                        && paper != null
                        && paper.getPassScore() != null
                        && record.getScore().compareTo(BigDecimal.valueOf(paper.getPassScore())) >= 0
        );
    }

    public TeacherWorkspaceDtos.AnswerRecordItem toAnswerRecordItem(AnswerRecord answerRecord) {
        Question question = answerRecord.getQuestion();
        return new TeacherWorkspaceDtos.AnswerRecordItem(
                answerRecord.getAnswerId(),
                answerRecord.getRecordId(),
                answerRecord.getQuestionId(),
                question != null && question.getQuestionType() != null ? question.getQuestionType().name() : null,
                question != null ? question.getContent() : null,
                question != null ? question.getOptionA() : null,
                question != null ? question.getOptionB() : null,
                question != null ? question.getOptionC() : null,
                question != null ? question.getOptionD() : null,
                answerRecord.getStudentAnswer(),
                question != null ? question.getCorrectAnswer() : null,
                question != null ? question.getAnalysis() : null,
                answerRecord.getScore(),
                Boolean.TRUE.equals(answerRecord.getIsCorrect())
        );
    }

    public TeacherWorkspaceDtos.StudentRecordSummary buildStudentSummary(List<ExamRecord> records) {
        ExamRecordStatisticsAssembler.RecordSummary recordSummary = statisticsAssembler.summarizeRecords(records);
        return new TeacherWorkspaceDtos.StudentRecordSummary(
                recordSummary.getRecordCount(),
                recordSummary.getSubmittedCount(),
                recordSummary.getAverageScore()
        );
    }

    private int resolveQuestionCount(Paper paper) {
        int optimizedCount = paper.getSingleCount() + paper.getMultipleCount() + paper.getJudgeCount() + paper.getBlankCount();
        if (optimizedCount > 0) {
            return optimizedCount;
        }
        return paper.getQuestions() == null ? 0 : paper.getQuestions().size();
    }

    private long calculateDurationSeconds(ExamRecord record) {
        if (record.getStartTime() == null) {
            return 0;
        }

        LocalDateTime endTime = record.getSubmitTime() != null
                ? record.getSubmitTime()
                : record.getEndTime();
        if (endTime == null) {
            return 0;
        }

        return Math.max(0, Duration.between(record.getStartTime(), endTime).getSeconds());
    }
}
