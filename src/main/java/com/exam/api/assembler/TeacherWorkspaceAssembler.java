package com.exam.api.assembler;

import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.User;
import com.exam.model.enums.ExamStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TeacherWorkspaceAssembler {

    public Map<String, Object> toTeacherPaperItem(Paper paper) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("paperId", paper.getPaperId());
        item.put("paperName", paper.getPaperName());
        item.put("subject", paper.getSubject());
        item.put("totalScore", paper.getTotalScore());
        item.put("duration", paper.getDuration());
        item.put("passScore", paper.getPassScore());
        item.put("questionCount", resolveQuestionCount(paper));
        item.put("published", Boolean.TRUE.equals(paper.getIsPublished()));
        item.put("description", paper.getDescription());
        item.put("createTime", paper.getCreateTime());
        item.put("updateTime", paper.getUpdateTime());
        return item;
    }

    public Map<String, Object> toTeacherStudentItem(User student, List<ExamRecord> records) {
        Map<String, Object> summary = buildStudentSummary(records);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("userId", student.getUserId());
        item.put("realName", student.getRealName());
        item.put("loginId", student.getLoginId());
        item.put("email", student.getEmail());
        item.put("phone", student.getPhone());
        item.put("gender", student.getGender());
        item.put("status", student.getStatus());
        item.put("createTime", student.getCreateTime());
        item.put("updateTime", student.getUpdateTime());
        item.put("recordCount", summary.get("recordCount"));
        item.put("submittedCount", summary.get("submittedCount"));
        item.put("averageScore", summary.get("averageScore"));
        return item;
    }

    public Map<String, Object> toTeacherStudentRecordItem(ExamRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("recordId", record.getRecordId());
        item.put("paperId", record.getPaperId());
        item.put("paperName", record.getPaper() != null ? record.getPaper().getPaperName() : null);
        item.put("status", record.getStatus() == null ? null : record.getStatus().name());
        item.put("score", record.getScore());
        item.put("startTime", record.getStartTime());
        item.put("submitTime", record.getSubmitTime());
        item.put("durationSeconds", calculateDurationSeconds(record));
        return item;
    }

    public Map<String, Object> toQuestionItem(Question question) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("questionId", question.getQuestionId());
        item.put("questionType", question.getQuestionType() == null ? null : question.getQuestionType().name());
        item.put("subject", question.getSubject());
        item.put("content", question.getContent());
        item.put("correctAnswer", question.getCorrectAnswer());
        item.put("score", question.getScore());
        item.put("difficulty", question.getDifficulty() == null ? null : question.getDifficulty().name());
        return item;
    }

    public Map<String, Object> toQuestionDetailItem(Question question) {
        Map<String, Object> item = toQuestionItem(question);
        item.put("optionA", question.getOptionA());
        item.put("optionB", question.getOptionB());
        item.put("optionC", question.getOptionC());
        item.put("optionD", question.getOptionD());
        item.put("analysis", question.getAnalysis());
        return item;
    }

    public Map<String, Object> toTeacherStudentRecordDetailItem(
            ExamRecord record,
            Paper paper,
            List<com.exam.model.AnswerRecord> answerRecords,
            long answeredCount,
            long correctCount,
            long wrongCount
    ) {
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
        item.put("questionCount", answerRecords.size());
        item.put("answeredCount", answeredCount);
        item.put("correctCount", correctCount);
        item.put("wrongCount", wrongCount);
        item.put("passed",
                record.getScore() != null
                        && paper != null
                        && paper.getPassScore() != null
                        && record.getScore().compareTo(BigDecimal.valueOf(paper.getPassScore())) >= 0);
        return item;
    }

    public Map<String, Object> toAnswerRecordItem(com.exam.model.AnswerRecord answerRecord) {
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

    public Map<String, Object> buildStudentSummary(List<ExamRecord> records) {
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
