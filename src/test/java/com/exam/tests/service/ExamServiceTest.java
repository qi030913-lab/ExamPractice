package com.exam.tests.service;

import com.exam.dao.ExamRecordDao;
import com.exam.dao.PaperDao;
import com.exam.dao.QuestionDao;
import com.exam.exception.BusinessException;
import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.Question;
import com.exam.model.enums.ExamStatus;
import com.exam.model.enums.QuestionType;
import com.exam.service.ExamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class ExamServiceTest {
    private ExamService examService;
    private ExamRecordDao examRecordDao;
    private PaperDao paperDao;
    private QuestionDao questionDao;

    @BeforeEach
    void setUp() {
        examRecordDao = mock(ExamRecordDao.class);
        paperDao = mock(PaperDao.class);
        questionDao = mock(QuestionDao.class);
        examService = new ExamService(examRecordDao, paperDao, questionDao);
    }

    @Test
    void submitExamShouldCalculateScoreAndPersistAnswers() {
        ExamRecord record = new ExamRecord(11, 101);
        record.setRecordId(5001);
        record.setStatus(ExamStatus.IN_PROGRESS);

        Question single = question(1, QuestionType.SINGLE, "A", 5);
        Question multiple = question(2, QuestionType.MULTIPLE, "AC", 10);
        Question judge = question(3, QuestionType.JUDGE, "T", 5);

        Map<Integer, String> answers = new HashMap<>();
        answers.put(1, "A");
        answers.put(2, "CA");
        answers.put(3, "F");

        when(examRecordDao.findByIdForUpdate(5001)).thenReturn(record);
        when(questionDao.findByPaperId(101)).thenReturn(List.of(single, multiple, judge));
        when(examRecordDao.update(any(ExamRecord.class))).thenReturn(1);

        BigDecimal score = examService.submitExam(5001, answers);

        assertEquals(new BigDecimal("15"), score);

        ArgumentCaptor<List<AnswerRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(examRecordDao).insertAnswerRecordsBatch(captor.capture());
        assertEquals(3, captor.getValue().size());
        verify(examRecordDao).update(any(ExamRecord.class));
    }

    @Test
    void submitExamShouldFailWhenAlreadySubmitted() {
        ExamRecord record = new ExamRecord(11, 101);
        record.setRecordId(5002);
        record.setStatus(ExamStatus.SUBMITTED);

        when(examRecordDao.findByIdForUpdate(5002)).thenReturn(record);

        assertThrows(BusinessException.class, () -> examService.submitExam(5002, Map.of()));
        verify(examRecordDao, never()).insertAnswerRecordsBatch(any());
    }

    @Test
    void submitExamShouldFailWhenRecordTimedOut() {
        ExamRecord record = new ExamRecord(11, 101);
        record.setRecordId(5003);
        record.setStatus(ExamStatus.TIMEOUT);

        when(examRecordDao.findByIdForUpdate(5003)).thenReturn(record);

        assertThrows(BusinessException.class, () -> examService.submitExam(5003, Map.of()));
        verify(examRecordDao, never()).insertAnswerRecordsBatch(any());
    }

    @Test
    void getStudentExamRecordsPaginatedShouldValidateInput() {
        assertThrows(BusinessException.class, () -> examService.getStudentExamRecordsPaginated(null, 1, 10));
        assertThrows(BusinessException.class, () -> examService.getStudentExamRecordsPaginated(1, 0, 10));
        assertThrows(BusinessException.class, () -> examService.getStudentExamRecordsPaginated(1, 1, 0));
        assertThrows(BusinessException.class, () -> examService.getStudentExamRecordsPaginated(1, 1, 201));
    }

    @Test
    void getStudentExamRecordsByStudentIdsShouldDelegateBatchQuery() {
        ExamRecord record = new ExamRecord(11, 101);
        Map<Integer, List<ExamRecord>> expected = Map.of(11, List.of(record), 12, List.of());
        when(examRecordDao.findByStudentIdsWithPaper(List.of(11, 12))).thenReturn(expected);

        Map<Integer, List<ExamRecord>> actual = examService.getStudentExamRecordsByStudentIds(List.of(11, 12));

        assertSame(expected, actual);
        verify(examRecordDao).findByStudentIdsWithPaper(List.of(11, 12));
    }

    @Test
    void getStudentExamRecordsByStudentIdsShouldRejectNullStudentId() {
        assertThrows(BusinessException.class, () -> examService.getStudentExamRecordsByStudentIds(java.util.Arrays.asList(11, null)));
    }

    @Test
    void startOrResumeExamShouldReturnExistingInProgressRecord() {
        com.exam.model.Paper paper = new com.exam.model.Paper();
        paper.setPaperId(101);

        ExamRecord existingRecord = new ExamRecord(11, 101);
        existingRecord.setRecordId(8001);
        existingRecord.setStatus(ExamStatus.IN_PROGRESS);

        when(paperDao.findById(101)).thenReturn(paper);
        when(examRecordDao.findInProgressByStudentIdAndPaperId(11, 101, ExamStatus.IN_PROGRESS)).thenReturn(existingRecord);

        ExamService.ExamStartResult result = examService.startOrResumeExam(11, 101);

        assertTrue(result.isResumed());
        assertEquals(8001, result.getRecord().getRecordId());
        verify(examRecordDao, never()).insert(any(ExamRecord.class));
    }

    @Test
    void startOrResumeExamShouldCreateRecordWhenNoInProgressRecordExists() {
        com.exam.model.Paper paper = new com.exam.model.Paper();
        paper.setPaperId(101);

        when(paperDao.findById(101)).thenReturn(paper);
        when(examRecordDao.findInProgressByStudentIdAndPaperId(11, 101, ExamStatus.IN_PROGRESS)).thenReturn(null);
        when(examRecordDao.insert(any(ExamRecord.class))).thenReturn(8002);

        ExamService.ExamStartResult result = examService.startOrResumeExam(11, 101);

        assertFalse(result.isResumed());
        assertEquals(8002, result.getRecord().getRecordId());
        verify(examRecordDao).insert(any(ExamRecord.class));
    }

    @Test
    void timeoutSubmitShouldSettleBlankAnswersAndUpdateRecord() {
        ExamRecord record = new ExamRecord(11, 101);
        record.setRecordId(7001);
        record.setStatus(ExamStatus.IN_PROGRESS);

        Question single = question(1, QuestionType.SINGLE, "A", 5);
        Question judge = question(2, QuestionType.JUDGE, "T", 5);

        when(examRecordDao.findByIdForUpdate(7001)).thenReturn(record);
        when(questionDao.findByPaperId(101)).thenReturn(List.of(single, judge));
        when(examRecordDao.update(any(ExamRecord.class))).thenReturn(1);

        assertDoesNotThrow(() -> examService.timeoutSubmit(7001));

        ArgumentCaptor<List<AnswerRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(examRecordDao).insertAnswerRecordsBatch(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertTrue(captor.getValue().stream().allMatch(answer -> answer.getStudentAnswer() == null));

        ArgumentCaptor<ExamRecord> recordCaptor = ArgumentCaptor.forClass(ExamRecord.class);
        verify(examRecordDao).update(recordCaptor.capture());
        assertEquals(ExamStatus.TIMEOUT, recordCaptor.getValue().getStatus());
        assertEquals(BigDecimal.ZERO, recordCaptor.getValue().getScore());
    }

    @Test
    void timeoutSubmitShouldReturnWhenRecordNotInProgress() {
        ExamRecord record = new ExamRecord(11, 101);
        record.setRecordId(7002);
        record.setStatus(ExamStatus.SUBMITTED);

        when(examRecordDao.findByIdForUpdate(7002)).thenReturn(record);

        assertDoesNotThrow(() -> examService.timeoutSubmit(7002));

        verify(examRecordDao, never()).insertAnswerRecordsBatch(any());
        verify(examRecordDao, never()).update(any(ExamRecord.class));
    }

    private static Question question(int id, QuestionType type, String answer, int score) {
        Question q = new Question();
        q.setQuestionId(id);
        q.setQuestionType(type);
        q.setSubject("Java");
        q.setContent("content");
        q.setCorrectAnswer(answer);
        q.setScore(score);
        q.setOptionA("A");
        q.setOptionB("B");
        return q;
    }
}
