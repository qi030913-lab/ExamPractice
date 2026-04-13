package com.exam.tests.service;

import com.exam.dao.PaperDao;
import com.exam.dao.QuestionDao;
import com.exam.exception.BusinessException;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.enums.QuestionType;
import com.exam.service.PaperService;
import com.exam.util.DBUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PaperServiceTest {
    private PaperService paperService;
    private PaperDao paperDao;
    private QuestionDao questionDao;

    @BeforeEach
    void setUp() {
        paperDao = mock(PaperDao.class);
        questionDao = mock(QuestionDao.class);
        paperService = new PaperService(paperDao, questionDao);
    }

    @Test
    void createPaperShouldDeduplicateQuestionIdsAndCommit() throws Exception {
        Paper paper = buildPaper();
        Question q1 = buildQuestion(1, 5);
        Question q2 = buildQuestion(2, 10);

        when(questionDao.findByIds(any())).thenReturn(Map.of(1, q1, 2, q2));

        Connection conn = mock(Connection.class);
        when(paperDao.insert(eq(conn), eq(paper))).thenReturn(200);

        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(() -> DBUtil.getConnection()).thenReturn(conn);

            int paperId = paperService.createPaper(paper, List.of(1, 2, 1));

            assertEquals(200, paperId);
            assertEquals(15, paper.getTotalScore());
            verify(paperDao).addPaperQuestionsBatch(eq(conn), eq(200), eq(List.of(1, 2)));
            verify(conn).commit();
            verify(conn, never()).rollback();
        }
    }

    @Test
    void createPaperShouldRollbackWhenBatchInsertFails() throws Exception {
        Paper paper = buildPaper();
        Question q1 = buildQuestion(1, 5);

        when(questionDao.findByIds(any())).thenReturn(Map.of(1, q1));

        Connection conn = mock(Connection.class);
        when(paperDao.insert(eq(conn), eq(paper))).thenReturn(201);
        doThrow(new RuntimeException("db fail"))
                .when(paperDao).addPaperQuestionsBatch(eq(conn), eq(201), eq(List.of(1)));

        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(() -> DBUtil.getConnection()).thenReturn(conn);

            assertThrows(RuntimeException.class, () -> paperService.createPaper(paper, List.of(1)));
            verify(conn).rollback();
        }
    }

    @Test
    void createPaperShouldRejectUnsupportedQuestionType() {
        Paper paper = buildPaper();
        Question unsupported = buildQuestion(1, 5);
        unsupported.setQuestionType(QuestionType.BLANK);
        when(questionDao.findByIds(any())).thenReturn(Map.of(1, unsupported));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paperService.createPaper(paper, List.of(1))
        );

        assertTrue(exception.getMessage().contains("BLANK"));
        verify(paperDao, never()).insert(any(Connection.class), any(Paper.class));
    }

    @Test
    void importPaperShouldCreateAndReuseQuestionsInSingleTransaction() throws Exception {
        Paper paper = buildPaper();
        Question existingQuestion = buildQuestion(10, 5);
        existingQuestion.setContent("existing");

        Question importedExisting = buildQuestion(0, 5);
        importedExisting.setContent("existing");
        Question importedNew = buildQuestion(0, 10);
        importedNew.setContent("new-one");

        Connection conn = mock(Connection.class);
        when(questionDao.findByExactSignature(eq(conn), eq("Java"), eq(QuestionType.SINGLE), eq("existing"), eq("A")))
                .thenReturn(existingQuestion);
        when(questionDao.findByExactSignature(eq(conn), eq("Java"), eq(QuestionType.SINGLE), eq("new-one"), eq("A")))
                .thenReturn(null);
        when(questionDao.insert(eq(conn), same(importedNew))).thenReturn(20);
        when(paperDao.insert(eq(conn), eq(paper))).thenReturn(300);

        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(conn);

            PaperService.ImportPaperResult result = paperService.importPaper(paper, List.of(importedExisting, importedNew));

            assertEquals(300, result.getPaperId());
            assertEquals(2, result.getSourceQuestionCount());
            assertEquals(2, result.getLinkedQuestionCount());
            assertEquals(1, result.getCreatedQuestionCount());
            assertEquals(1, result.getReusedQuestionCount());
            assertEquals(15, paper.getTotalScore());
            verify(paperDao).addPaperQuestionsBatch(eq(conn), eq(300), eq(List.of(10, 20)));
            verify(conn).commit();
            verify(conn, never()).rollback();
        }
    }

    @Test
    void importPaperShouldRollbackWhenPaperInsertFails() throws Exception {
        Paper paper = buildPaper();
        Question imported = buildQuestion(0, 5);
        imported.setContent("imported");

        Connection conn = mock(Connection.class);
        when(questionDao.findByExactSignature(eq(conn), eq("Java"), eq(QuestionType.SINGLE), eq("imported"), eq("A")))
                .thenReturn(null);
        when(questionDao.insert(eq(conn), same(imported))).thenReturn(21);
        doThrow(new RuntimeException("paper insert fail"))
                .when(paperDao).insert(eq(conn), eq(paper));

        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(conn);

            assertThrows(RuntimeException.class, () -> paperService.importPaper(paper, List.of(imported)));
            verify(conn).rollback();
        }
    }

    private static Paper buildPaper() {
        Paper paper = new Paper();
        paper.setPaperName("Java Test");
        paper.setSubject("Java");
        paper.setDuration(90);
        paper.setPassScore(60);
        return paper;
    }

    private static Question buildQuestion(int id, int score) {
        Question q = new Question();
        q.setQuestionId(id);
        q.setQuestionType(QuestionType.SINGLE);
        q.setSubject("Java");
        q.setContent("content");
        q.setCorrectAnswer("A");
        q.setScore(score);
        q.setOptionA("A");
        q.setOptionB("B");
        return q;
    }
}
