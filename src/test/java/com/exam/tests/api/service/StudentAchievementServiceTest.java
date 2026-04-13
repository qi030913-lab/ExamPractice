package com.exam.tests.api.service;

import com.exam.api.service.StudentAchievementService;
import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.enums.ExamStatus;
import com.exam.model.enums.QuestionType;
import com.exam.service.ExamService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StudentAchievementServiceTest {

    @Test
    void buildSnapshotShouldAggregateCompletedRecords() {
        ExamService examService = mock(ExamService.class);
        StudentAchievementService service = new StudentAchievementService(examService);

        Paper javaPaper = new Paper();
        javaPaper.setPaperId(101);
        javaPaper.setPaperName("Java期中考试");
        javaPaper.setSubject("Java");
        javaPaper.setTotalScore(100);
        javaPaper.setPassScore(60);

        ExamRecord submitted = new ExamRecord(7, 101);
        submitted.setRecordId(5001);
        submitted.setPaper(javaPaper);
        submitted.setStatus(ExamStatus.SUBMITTED);
        submitted.setScore(new BigDecimal("80"));
        submitted.setStartTime(LocalDateTime.now().minusHours(2));
        submitted.setSubmitTime(LocalDateTime.now().minusHours(1));

        ExamRecord timeout = new ExamRecord(7, 101);
        timeout.setRecordId(5002);
        timeout.setPaper(javaPaper);
        timeout.setStatus(ExamStatus.TIMEOUT);
        timeout.setScore(BigDecimal.ZERO);
        timeout.setStartTime(LocalDateTime.now().minusMinutes(45));
        timeout.setEndTime(LocalDateTime.now().minusMinutes(15));

        ExamRecord inProgress = new ExamRecord(7, 102);
        inProgress.setRecordId(5003);
        inProgress.setStatus(ExamStatus.IN_PROGRESS);

        AnswerRecord correct = new AnswerRecord(5001, 1, "A");
        correct.setIsCorrect(true);
        correct.setQuestion(question(1, QuestionType.SINGLE));

        AnswerRecord wrong = new AnswerRecord(5001, 2, "B");
        wrong.setIsCorrect(false);
        wrong.setQuestion(question(2, QuestionType.MULTIPLE));

        AnswerRecord blank = new AnswerRecord(5002, 3, null);
        blank.setIsCorrect(false);
        blank.setQuestion(question(3, QuestionType.JUDGE));

        when(examService.getStudentExamRecordsOptimized(7)).thenReturn(List.of(submitted, timeout, inProgress));
        when(examService.getAnswerRecordsBatch(List.of(5001, 5002))).thenReturn(Map.of(
                5001, List.of(correct, wrong),
                5002, List.of(blank)
        ));

        StudentAchievementService.StudentAchievementSnapshot snapshot = service.buildSnapshot(7);

        assertEquals(3, snapshot.getSummary().getTotalExamCount());
        assertEquals(2, snapshot.getSummary().getCompletedExamCount());
        assertEquals(1L, snapshot.getSummary().getInProgressCount());
        assertEquals(40.0d, snapshot.getSummary().getAverageScore());
        assertEquals(new BigDecimal("80"), snapshot.getSummary().getBestScore());
        assertEquals(1, snapshot.getSummary().getPassCount());
        assertEquals(1, snapshot.getSummary().getFailCount());
        assertEquals(3L, snapshot.getSummary().getTotalQuestionCount());
        assertEquals(2L, snapshot.getSummary().getTotalAnsweredCount());
        assertEquals(1L, snapshot.getSummary().getTotalCorrectCount());
        assertEquals(33.33d, snapshot.getSummary().getAccuracyRate());
        assertEquals(2, snapshot.getScoreTrend().size());
        assertEquals(3, snapshot.getQuestionTypeAccuracy().size());
        assertEquals(1, snapshot.getSubjectPerformance().size());
        assertNotNull(snapshot.getLatestUpdatedAt());
    }

    private static Question question(int id, QuestionType questionType) {
        Question question = new Question();
        question.setQuestionId(id);
        question.setQuestionType(questionType);
        return question;
    }
}
