package com.exam.api.assembler;

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

    public Map<String, Object> buildPaperSummary(List<Paper> papers, List<ExamRecord> records) {
        long completedCount = records.stream()
                .filter(record -> record.getStatus() == ExamStatus.SUBMITTED || record.getStatus() == ExamStatus.TIMEOUT)
                .count();
        long inProgressCount = records.stream()
                .filter(record -> record.getStatus() == ExamStatus.IN_PROGRESS)
                .count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("paperCount", papers.size());
        summary.put("completedCount", completedCount);
        summary.put("inProgressCount", inProgressCount);
        return summary;
    }

    public Map<String, Object> buildRecordSummary(List<ExamRecord> records) {
        long submittedCount = records.stream()
                .filter(record -> record.getStatus() == ExamStatus.SUBMITTED || record.getStatus() == ExamStatus.TIMEOUT)
                .count();
        double averageScore = records.stream()
                .map(ExamRecord::getScore)
                .filter(score -> score != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("recordCount", records.size());
        summary.put("submittedCount", submittedCount);
        summary.put("averageScore", averageScore);
        return summary;
    }

    public Map<String, Object> toStudentPaperItem(Paper paper, ExamRecord latestRecord) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("paperId", paper.getPaperId());
        item.put("paperName", paper.getPaperName());
        item.put("subject", paper.getSubject());
        item.put("duration", paper.getDuration());
        item.put("totalScore", paper.getTotalScore());
        item.put("passScore", paper.getPassScore());
        item.put("description", paper.getDescription());
        item.put("questionCount", resolveQuestionCount(paper));
        item.put("published", Boolean.TRUE.equals(paper.getIsPublished()));
        item.put("hasInProgressRecord", latestRecord != null && latestRecord.getStatus() == ExamStatus.IN_PROGRESS);
        if (latestRecord != null) {
            item.put("latestRecord", toStudentRecordCard(latestRecord));
        }
        return item;
    }

    public Map<String, Object> toQuestionExamItem(Question question) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("questionId", question.getQuestionId());
        item.put("questionType", question.getQuestionType() == null ? null : question.getQuestionType().name());
        item.put("subject", question.getSubject());
        item.put("content", question.getContent());
        item.put("optionA", question.getOptionA());
        item.put("optionB", question.getOptionB());
        item.put("optionC", question.getOptionC());
        item.put("optionD", question.getOptionD());
        item.put("score", question.getScore());
        return item;
    }

    public Map<String, Object> toExamLifecycleRecordItem(ExamRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", record.getRecordId());
        item.put("studentId", record.getStudentId());
        item.put("paperId", record.getPaperId());
        item.put("status", record.getStatus() == null ? null : record.getStatus().name());
        item.put("startTime", record.getStartTime());
        item.put("submitTime", record.getSubmitTime());
        item.put("durationSeconds", calculateDurationSeconds(record));
        item.put("resumeAvailable", record.getStatus() == ExamStatus.IN_PROGRESS);
        return item;
    }

    public Map<String, Object> toStudentRecordCard(ExamRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", record.getRecordId());
        item.put("paperId", record.getPaperId());
        item.put("paperName", record.getPaper() != null ? record.getPaper().getPaperName() : null);
        item.put("status", record.getStatus() != null ? record.getStatus().name() : null);
        item.put("score", record.getScore());
        item.put("submitTime", record.getSubmitTime());
        item.put("startTime", record.getStartTime());
        item.put("durationSeconds", calculateDurationSeconds(record));
        item.put("resumeAvailable", record.getStatus() == ExamStatus.IN_PROGRESS);
        return item;
    }

    public Map<String, Object> toStudentScoreRecordItem(ExamRecord record, List<AnswerRecord> answerRecords) {
        List<AnswerRecord> safeAnswers = answerRecords == null ? List.of() : answerRecords;
        long correctCount = countCorrectAnswers(safeAnswers);
        long wrongCount = countWrongAnswers(safeAnswers);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", record.getRecordId());
        item.put("paperId", record.getPaperId());
        item.put("paperName", record.getPaper() != null ? record.getPaper().getPaperName() : null);
        item.put("totalScore", record.getPaper() != null ? record.getPaper().getTotalScore() : null);
        item.put("score", record.getScore());
        item.put("status", record.getStatus() != null ? record.getStatus().name() : null);
        item.put("submitTime", record.getSubmitTime());
        item.put("startTime", record.getStartTime());
        item.put("durationSeconds", calculateDurationSeconds(record));
        item.put("correctCount", correctCount);
        item.put("wrongCount", wrongCount);
        item.put("resumeAvailable", record.getStatus() == ExamStatus.IN_PROGRESS);
        return item;
    }

    public Map<String, Object> toStudentRecordDetailItem(
            ExamRecord record,
            Paper paper,
            List<AnswerRecord> answerRecords,
            long answeredCount,
            long correctCount,
            long wrongCount
    ) {
        List<AnswerRecord> safeAnswers = answerRecords == null ? List.of() : answerRecords;

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", record.getRecordId());
        item.put("paperId", record.getPaperId());
        item.put("paperName", paper != null ? paper.getPaperName() : null);
        item.put("subject", paper != null ? paper.getSubject() : null);
        item.put("totalScore", paper != null ? paper.getTotalScore() : null);
        item.put("passScore", paper != null ? paper.getPassScore() : null);
        item.put("score", record.getScore());
        item.put("status", record.getStatus() == null ? null : record.getStatus().name());
        item.put("startTime", record.getStartTime());
        item.put("submitTime", record.getSubmitTime());
        item.put("durationSeconds", calculateDurationSeconds(record));
        item.put("questionCount", safeAnswers.size());
        item.put("answeredCount", answeredCount);
        item.put("correctCount", correctCount);
        item.put("wrongCount", wrongCount);
        item.put("resumeAvailable", record.getStatus() == ExamStatus.IN_PROGRESS);
        item.put("passed", isPassed(record.getScore(), paper));
        return item;
    }

    public Map<String, Object> toAnswerRecordItem(AnswerRecord answerRecord) {
        Question question = answerRecord.getQuestion();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("answerId", answerRecord.getAnswerId());
        item.put("recordId", answerRecord.getRecordId());
        item.put("questionId", answerRecord.getQuestionId());
        item.put("questionType", question != null && question.getQuestionType() != null ? question.getQuestionType().name() : null);
        item.put("content", question != null ? question.getContent() : null);
        item.put("optionA", question != null ? question.getOptionA() : null);
        item.put("optionB", question != null ? question.getOptionB() : null);
        item.put("optionC", question != null ? question.getOptionC() : null);
        item.put("optionD", question != null ? question.getOptionD() : null);
        item.put("studentAnswer", answerRecord.getStudentAnswer());
        item.put("correctAnswer", question != null ? question.getCorrectAnswer() : null);
        item.put("analysis", question != null ? question.getAnalysis() : null);
        item.put("score", answerRecord.getScore());
        item.put("isCorrect", Boolean.TRUE.equals(answerRecord.getIsCorrect()));
        return item;
    }

    public Map<String, Object> toSubmitResultItem(
            ExamRecord record,
            Paper paper,
            BigDecimal score,
            int questionCount,
            long answeredCount,
            long correctCount,
            long wrongCount
    ) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", record.getRecordId());
        item.put("paperId", record.getPaperId());
        item.put("paperName", paper != null ? paper.getPaperName() : null);
        item.put("subject", paper != null ? paper.getSubject() : null);
        item.put("score", score);
        item.put("totalScore", paper != null ? paper.getTotalScore() : null);
        item.put("passScore", paper != null ? paper.getPassScore() : null);
        item.put("passed", isPassed(score, paper));
        item.put("status", record.getStatus() != null ? record.getStatus().name() : null);
        item.put("submitTime", record.getSubmitTime());
        item.put("durationSeconds", calculateDurationSeconds(record));
        item.put("questionCount", questionCount);
        item.put("answeredCount", answeredCount);
        item.put("correctCount", correctCount);
        item.put("wrongCount", wrongCount);
        return item;
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

    private long countCorrectAnswers(List<AnswerRecord> answerRecords) {
        return answerRecords.stream()
                .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();
    }

    private long countWrongAnswers(List<AnswerRecord> answerRecords) {
        return answerRecords.stream()
                .filter(answer -> answer.getStudentAnswer() != null && !answer.getStudentAnswer().trim().isEmpty())
                .filter(answer -> !Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();
    }
}
