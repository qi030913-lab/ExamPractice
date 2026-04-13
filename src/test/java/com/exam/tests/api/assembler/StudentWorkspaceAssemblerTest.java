package com.exam.tests.api.assembler;

import com.exam.api.assembler.StudentWorkspaceAssembler;
import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.enums.ExamStatus;
import com.exam.model.enums.QuestionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentWorkspaceAssemblerTest {
    private final StudentWorkspaceAssembler assembler = new StudentWorkspaceAssembler();

    @Test
    void buildPaperSummaryShouldCountCompletedAndInProgressRecords() {
        Paper paper = new Paper();
        ExamRecord submitted = new ExamRecord(1, 101);
        submitted.setStatus(ExamStatus.SUBMITTED);
        ExamRecord timeout = new ExamRecord(1, 102);
        timeout.setStatus(ExamStatus.TIMEOUT);
        ExamRecord inProgress = new ExamRecord(1, 103);
        inProgress.setStatus(ExamStatus.IN_PROGRESS);

        Map<String, Object> summary = assembler.buildPaperSummary(List.of(paper), List.of(submitted, timeout, inProgress));

        assertEquals(1, summary.get("paperCount"));
        assertEquals(2L, summary.get("completedCount"));
        assertEquals(1L, summary.get("inProgressCount"));
    }

    @Test
    void toStudentPaperItemShouldIncludeLatestInProgressRecord() {
        Paper paper = new Paper();
        paper.setPaperId(2001);
        paper.setPaperName("Java测试");
        paper.setSingleCount(2);
        paper.setMultipleCount(1);
        paper.setJudgeCount(1);

        ExamRecord latestRecord = new ExamRecord(11, 2001);
        latestRecord.setRecordId(9001);
        latestRecord.setStatus(ExamStatus.IN_PROGRESS);
        latestRecord.setStartTime(LocalDateTime.now().minusMinutes(10));
        latestRecord.setPaper(paper);

        Map<String, Object> item = assembler.toStudentPaperItem(paper, latestRecord);

        assertEquals(2001, item.get("paperId"));
        assertEquals(4, item.get("questionCount"));
        assertEquals(true, item.get("hasInProgressRecord"));
        assertNotNull(item.get("latestRecord"));
    }

    @Test
    void toStudentRecordDetailItemShouldComputePassedFlag() {
        ExamRecord record = new ExamRecord(8, 101);
        record.setRecordId(6001);
        record.setStatus(ExamStatus.SUBMITTED);
        record.setScore(new BigDecimal("88"));
        record.setStartTime(LocalDateTime.now().minusMinutes(40));
        record.setSubmitTime(LocalDateTime.now().minusMinutes(5));

        Paper paper = new Paper();
        paper.setPaperId(101);
        paper.setPaperName("期中考试");
        paper.setPassScore(60);
        paper.setTotalScore(100);
        paper.setSubject("Java");

        AnswerRecord answer = new AnswerRecord(6001, 1, "A");
        Question question = new Question();
        question.setQuestionId(1);
        question.setQuestionType(QuestionType.SINGLE);
        question.setContent("content");
        question.setCorrectAnswer("A");
        answer.setQuestion(question);
        answer.setIsCorrect(true);

        Map<String, Object> item = assembler.toStudentRecordDetailItem(record, paper, List.of(answer), 1, 1, 0);

        assertEquals(1, item.get("questionCount"));
        assertEquals(1L, item.get("answeredCount"));
        assertEquals(true, item.get("passed"));
    }

    @Test
    void calculateRemainingSecondsShouldStayWithinDurationWindow() {
        ExamRecord record = new ExamRecord(5, 3001);
        record.setStatus(ExamStatus.IN_PROGRESS);
        record.setStartTime(LocalDateTime.now().minusMinutes(10));

        Paper paper = new Paper();
        paper.setDuration(30);

        long remainingSeconds = assembler.calculateRemainingSeconds(record, paper);
        LocalDateTime deadline = assembler.calculateDeadlineTime(record, paper);

        assertTrue(remainingSeconds > 0);
        assertTrue(remainingSeconds <= 20 * 60);
        assertEquals(record.getStartTime().plusMinutes(30), deadline);
    }

    @Test
    void resolveLatestRecordByPaperIdShouldPreferLargerRecordId() {
        ExamRecord earlier = new ExamRecord(5, 900);
        earlier.setRecordId(100);
        ExamRecord later = new ExamRecord(5, 900);
        later.setRecordId(101);

        Map<Integer, ExamRecord> latest = assembler.resolveLatestRecordByPaperId(List.of(earlier, later));

        assertEquals(101, latest.get(900).getRecordId());
    }
}
