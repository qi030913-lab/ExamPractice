package com.exam.api.assembler;

import com.exam.api.dto.StudentWorkspaceDtos;
import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.enums.ExamStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class StudentWorkspaceAssembler {
    private final ExamRecordStatisticsAssembler statisticsAssembler;

    public StudentWorkspaceAssembler(ExamRecordStatisticsAssembler statisticsAssembler) {
        this.statisticsAssembler = statisticsAssembler;
    }

    public StudentWorkspaceDtos.PaperSummary buildPaperSummary(List<Paper> papers, List<ExamRecord> records) {
        long completedCount = records.stream()
                .filter(record -> record.getStatus() == ExamStatus.SUBMITTED || record.getStatus() == ExamStatus.TIMEOUT)
                .count();
        long inProgressCount = records.stream()
                .filter(record -> record.getStatus() == ExamStatus.IN_PROGRESS)
                .count();
        return new StudentWorkspaceDtos.PaperSummary(papers.size(), completedCount, inProgressCount);
    }

    public StudentWorkspaceDtos.RecordSummary buildRecordSummary(List<ExamRecord> records) {
        ExamRecordStatisticsAssembler.RecordSummary recordSummary = statisticsAssembler.summarizeRecords(records);
        return new StudentWorkspaceDtos.RecordSummary(
                recordSummary.getRecordCount(),
                recordSummary.getSubmittedCount(),
                recordSummary.getAverageScore()
        );
    }

    public StudentWorkspaceDtos.StudentPaperItem toStudentPaperItem(Paper paper, ExamRecord latestRecord) {
        return new StudentWorkspaceDtos.StudentPaperItem(
                paper.getPaperId(),
                paper.getPaperName(),
                paper.getSubject(),
                paper.getDuration(),
                paper.getTotalScore(),
                paper.getPassScore(),
                paper.getDescription(),
                resolveQuestionCount(paper),
                Boolean.TRUE.equals(paper.getIsPublished()),
                latestRecord != null && latestRecord.getStatus() == ExamStatus.IN_PROGRESS,
                latestRecord == null ? null : toStudentRecordCard(latestRecord)
        );
    }

    public StudentWorkspaceDtos.QuestionExamItem toQuestionExamItem(Question question) {
        return new StudentWorkspaceDtos.QuestionExamItem(
                question.getQuestionId(),
                question.getQuestionType() == null ? null : question.getQuestionType().name(),
                question.getSubject(),
                question.getContent(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getScore()
        );
    }

    public StudentWorkspaceDtos.ExamLifecycleRecordItem toExamLifecycleRecordItem(ExamRecord record) {
        return new StudentWorkspaceDtos.ExamLifecycleRecordItem(
                record.getRecordId(),
                record.getStudentId(),
                record.getPaperId(),
                record.getStatus() == null ? null : record.getStatus().name(),
                record.getStartTime(),
                record.getSubmitTime(),
                calculateDurationSeconds(record),
                record.getStatus() == ExamStatus.IN_PROGRESS
        );
    }

    public StudentWorkspaceDtos.StudentRecordCard toStudentRecordCard(ExamRecord record) {
        return new StudentWorkspaceDtos.StudentRecordCard(
                record.getRecordId(),
                record.getPaperId(),
                record.getPaper() != null ? record.getPaper().getPaperName() : null,
                record.getStatus() != null ? record.getStatus().name() : null,
                record.getScore(),
                record.getSubmitTime(),
                record.getStartTime(),
                calculateDurationSeconds(record),
                record.getStatus() == ExamStatus.IN_PROGRESS
        );
    }

    public StudentWorkspaceDtos.StudentScoreRecordItem toStudentScoreRecordItem(ExamRecord record, List<AnswerRecord> answerRecords) {
        ExamRecordStatisticsAssembler.AnswerSummary answerSummary = statisticsAssembler.summarizeAnswers(answerRecords);
        return new StudentWorkspaceDtos.StudentScoreRecordItem(
                record.getRecordId(),
                record.getPaperId(),
                record.getPaper() != null ? record.getPaper().getPaperName() : null,
                record.getPaper() != null ? record.getPaper().getTotalScore() : null,
                record.getScore(),
                record.getStatus() != null ? record.getStatus().name() : null,
                record.getSubmitTime(),
                record.getStartTime(),
                calculateDurationSeconds(record),
                answerSummary.getCorrectCount(),
                answerSummary.getWrongCount(),
                record.getStatus() == ExamStatus.IN_PROGRESS
        );
    }

    public StudentWorkspaceDtos.StudentRecordDetailItem toStudentRecordDetailItem(
            ExamRecord record,
            Paper paper,
            List<AnswerRecord> answerRecords
    ) {
        ExamRecordStatisticsAssembler.AnswerSummary answerSummary = statisticsAssembler.summarizeAnswers(answerRecords);
        return toStudentRecordDetailItem(
                record,
                paper,
                answerRecords,
                answerSummary.getAnsweredCount(),
                answerSummary.getCorrectCount(),
                answerSummary.getWrongCount()
        );
    }

    public StudentWorkspaceDtos.StudentRecordDetailItem toStudentRecordDetailItem(
            ExamRecord record,
            Paper paper,
            List<AnswerRecord> answerRecords,
            long answeredCount,
            long correctCount,
            long wrongCount
    ) {
        List<AnswerRecord> safeAnswers = answerRecords == null ? List.of() : answerRecords;
        return new StudentWorkspaceDtos.StudentRecordDetailItem(
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
                safeAnswers.size(),
                answeredCount,
                correctCount,
                wrongCount,
                record.getStatus() == ExamStatus.IN_PROGRESS,
                isPassed(record.getScore(), paper)
        );
    }

    public StudentWorkspaceDtos.AnswerRecordItem toAnswerRecordItem(AnswerRecord answerRecord) {
        Question question = answerRecord.getQuestion();
        return new StudentWorkspaceDtos.AnswerRecordItem(
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

    public StudentWorkspaceDtos.SubmitResultItem toSubmitResultItem(
            ExamRecord record,
            Paper paper,
            BigDecimal score,
            List<AnswerRecord> answerRecords
    ) {
        ExamRecordStatisticsAssembler.AnswerSummary answerSummary = statisticsAssembler.summarizeAnswers(answerRecords);
        return toSubmitResultItem(
                record,
                paper,
                score,
                answerSummary.getQuestionCount(),
                answerSummary.getAnsweredCount(),
                answerSummary.getCorrectCount(),
                answerSummary.getWrongCount()
        );
    }

    public StudentWorkspaceDtos.SubmitResultItem toSubmitResultItem(
            ExamRecord record,
            Paper paper,
            BigDecimal score,
            int questionCount,
            long answeredCount,
            long correctCount,
            long wrongCount
    ) {
        return new StudentWorkspaceDtos.SubmitResultItem(
                record.getRecordId(),
                record.getPaperId(),
                paper != null ? paper.getPaperName() : null,
                paper != null ? paper.getSubject() : null,
                score,
                paper != null ? paper.getTotalScore() : null,
                paper != null ? paper.getPassScore() : null,
                isPassed(score, paper),
                record.getStatus() != null ? record.getStatus().name() : null,
                record.getSubmitTime(),
                calculateDurationSeconds(record),
                questionCount,
                answeredCount,
                correctCount,
                wrongCount
        );
    }

    public Map<Integer, ExamRecord> resolveLatestRecordByPaperId(List<ExamRecord> records) {
        Map<Integer, ExamRecord> latest = new LinkedHashMap<>();
        for (ExamRecord record : records) {
            Integer paperId = record.getPaperId();
            if (paperId == null) {
                continue;
            }

            ExamRecord existing = latest.get(paperId);
            if (existing == null || compareRecordOrder(record, existing) > 0) {
                latest.put(paperId, record);
            }
        }
        return latest;
    }

    public long calculateRemainingSeconds(ExamRecord record, Paper paper) {
        if (record.getStatus() != ExamStatus.IN_PROGRESS) {
            return 0;
        }
        if (record.getStartTime() == null || paper == null || paper.getDuration() == null) {
            return 0;
        }

        LocalDateTime deadline = record.getStartTime().plusMinutes(paper.getDuration());
        return Math.max(0, Duration.between(LocalDateTime.now(), deadline).getSeconds());
    }

    public LocalDateTime calculateDeadlineTime(ExamRecord record, Paper paper) {
        if (record.getStartTime() == null || paper == null || paper.getDuration() == null) {
            return null;
        }
        return record.getStartTime().plusMinutes(paper.getDuration());
    }

    private int compareRecordOrder(ExamRecord left, ExamRecord right) {
        Integer leftId = left.getRecordId();
        Integer rightId = right.getRecordId();
        if (leftId == null && rightId == null) {
            return 0;
        }
        if (leftId == null) {
            return -1;
        }
        if (rightId == null) {
            return 1;
        }
        return Integer.compare(leftId, rightId);
    }

    private int resolveQuestionCount(Paper paper) {
        int questionCount = paper.getSingleCount()
                + paper.getMultipleCount()
                + paper.getJudgeCount()
                + paper.getBlankCount();
        if (questionCount > 0) {
            return questionCount;
        }
        return paper.getQuestions() == null ? 0 : paper.getQuestions().size();
    }

    private long calculateDurationSeconds(ExamRecord record) {
        if (record.getStartTime() == null) {
            return 0;
        }

        LocalDateTime endTime = record.getSubmitTime() != null ? record.getSubmitTime() : record.getEndTime();
        if (endTime == null) {
            return 0;
        }

        return Math.max(0, Duration.between(record.getStartTime(), endTime).getSeconds());
    }

    private boolean isPassed(BigDecimal score, Paper paper) {
        return score != null
                && paper != null
                && paper.getPassScore() != null
                && score.compareTo(BigDecimal.valueOf(paper.getPassScore())) >= 0;
    }
}
