package com.exam.tests.dao;

import com.exam.dao.QuestionDao;
import com.exam.model.Question;
import com.exam.model.enums.QuestionType;
import com.exam.util.DBUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuestionDaoTest {

    @Test
    void updateShouldDefaultDifficultyToMediumWhenMissing() throws Exception {
        QuestionDao questionDao = new QuestionDao();
        Question question = buildQuestion();
        question.setDifficulty(null);

        Connection conn = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class)) {
            dbUtil.when(DBUtil::getConnection).thenReturn(conn);

            int rows = questionDao.update(question);

            assertEquals(1, rows);
            verify(pstmt).setString(10, "MEDIUM");
            verify(pstmt).setInt(12, 99);
        }
    }

    private static Question buildQuestion() {
        Question question = new Question();
        question.setQuestionId(99);
        question.setQuestionType(QuestionType.SINGLE);
        question.setSubject("Java");
        question.setContent("What is Java?");
        question.setOptionA("A");
        question.setOptionB("B");
        question.setCorrectAnswer("A");
        question.setScore(5);
        question.setAnalysis("analysis");
        return question;
    }
}
