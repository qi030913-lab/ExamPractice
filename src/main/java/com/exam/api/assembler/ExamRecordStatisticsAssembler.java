package com.exam.api.assembler;

import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.enums.ExamStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ExamRecordStatisticsAssembler {
    public RecordSummary summarizeRecords(List<ExamRecord> records) {
        List<ExamRecord> safeRecords = records == null ? List.of() : records;
        long submittedCount = safeRecords.stream()
                .filter(record -> record.getStatus() == ExamStatus.SUBMITTED || record.getStatus() == ExamStatus.TIMEOUT)
                .count();
        double averageScore = safeRecords.stream()
                .map(ExamRecord::getScore)
                .filter(score -> score != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0);
        return new RecordSummary(safeRecords.size(), submittedCount, averageScore);
    }

    public AnswerSummary summarizeAnswers(List<AnswerRecord> answerRecords) {
        List<AnswerRecord> safeAnswers = answerRecords == null ? List.of() : answerRecords;
        long answeredCount = safeAnswers.stream()
                .filter(answer -> answer.getStudentAnswer() != null && !answer.getStudentAnswer().trim().isEmpty())
                .count();
        long correctCount = safeAnswers.stream()
                .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();
        long wrongCount = safeAnswers.stream()
                .filter(answer -> answer.getStudentAnswer() != null && !answer.getStudentAnswer().trim().isEmpty())
                .filter(answer -> !Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();
        return new AnswerSummary(safeAnswers.size(), answeredCount, correctCount, wrongCount);
    }

    public static class RecordSummary {
        private final int recordCount;
        private final long submittedCount;
        private final double averageScore;

        public RecordSummary(int recordCount, long submittedCount, double averageScore) {
            this.recordCount = recordCount;
            this.submittedCount = submittedCount;
            this.averageScore = averageScore;
        }

        public int getRecordCount() {
            return recordCount;
        }

        public long getSubmittedCount() {
            return submittedCount;
        }

        public double getAverageScore() {
            return averageScore;
        }
    }

    public static class AnswerSummary {
        private final int questionCount;
        private final long answeredCount;
        private final long correctCount;
        private final long wrongCount;

        public AnswerSummary(int questionCount, long answeredCount, long correctCount, long wrongCount) {
            this.questionCount = questionCount;
            this.answeredCount = answeredCount;
            this.correctCount = correctCount;
            this.wrongCount = wrongCount;
        }

        public int getQuestionCount() {
            return questionCount;
        }

        public long getAnsweredCount() {
            return answeredCount;
        }

        public long getCorrectCount() {
            return correctCount;
        }

        public long getWrongCount() {
            return wrongCount;
        }
    }
}
