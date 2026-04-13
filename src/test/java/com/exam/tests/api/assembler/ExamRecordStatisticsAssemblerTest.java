package com.exam.tests.api.assembler;

import com.exam.api.assembler.ExamRecordStatisticsAssembler;
import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.enums.ExamStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExamRecordStatisticsAssemblerTest {
    private final ExamRecordStatisticsAssembler assembler = new ExamRecordStatisticsAssembler();

    @Test
    void summarizeRecordsShouldCountSubmittedAndAverageScore() {
        ExamRecord submitted = new ExamRecord(1, 11);
        submitted.setStatus(ExamStatus.SUBMITTED);
        submitted.setScore(new BigDecimal("80"));
        ExamRecord timeout = new ExamRecord(1, 12);
        timeout.setStatus(ExamStatus.TIMEOUT);
        timeout.setScore(new BigDecimal("60"));
        ExamRecord inProgress = new ExamRecord(1, 13);
        inProgress.setStatus(ExamStatus.IN_PROGRESS);

        ExamRecordStatisticsAssembler.RecordSummary summary = assembler.summarizeRecords(List.of(submitted, timeout, inProgress));

        assertEquals(3, summary.getRecordCount());
        assertEquals(2L, summary.getSubmittedCount());
        assertEquals(70.0d, summary.getAverageScore());
    }

    @Test
    void summarizeAnswersShouldCountAnsweredCorrectAndWrong() {
        AnswerRecord correct = new AnswerRecord(1, 101, "A");
        correct.setIsCorrect(true);
        AnswerRecord wrong = new AnswerRecord(1, 102, "B");
        wrong.setIsCorrect(false);
        AnswerRecord blank = new AnswerRecord(1, 103, " ");
        blank.setIsCorrect(false);

        ExamRecordStatisticsAssembler.AnswerSummary summary = assembler.summarizeAnswers(List.of(correct, wrong, blank));

        assertEquals(3, summary.getQuestionCount());
        assertEquals(2L, summary.getAnsweredCount());
        assertEquals(1L, summary.getCorrectCount());
        assertEquals(1L, summary.getWrongCount());
    }
}
