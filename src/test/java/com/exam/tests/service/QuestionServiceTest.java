package com.exam.tests.service;

import com.exam.dao.QuestionDao;
import com.exam.exception.BusinessException;
import com.exam.model.Question;
import com.exam.model.enums.QuestionType;
import com.exam.service.QuestionService;
import com.exam.tests.support.FieldInjector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuestionServiceTest {
    private QuestionService questionService;
    private QuestionDao questionDao;

    @BeforeEach
    void setUp() {
        questionService = new QuestionService();
        questionDao = mock(QuestionDao.class);
        FieldInjector.setField(questionService, "questionDao", questionDao);
    }

    @Test
    void addQuestionShouldThrowWhenSingleChoiceOptionsMissing() {
        Question question = new Question();
        question.setQuestionType(QuestionType.SINGLE);
        question.setSubject("Java");
        question.setContent("content");
        question.setCorrectAnswer("A");
        question.setScore(5);

        assertThrows(BusinessException.class, () -> questionService.addQuestion(question));
    }

    @Test
    void addQuestionShouldCallDaoWhenValid() {
        Question question = validQuestion();
        when(questionDao.insert(question)).thenReturn(10);

        int id = questionService.addQuestion(question);

        assertEquals(10, id);
        verify(questionDao).insert(question);
    }

    @Test
    void batchAddQuestionsShouldInsertAll() {
        Question q1 = validQuestion();
        Question q2 = validQuestion();
        q2.setContent("another");

        when(questionDao.insert(any(Question.class))).thenReturn(1, 2);

        List<Integer> ids = questionService.batchAddQuestions(List.of(q1, q2));

        assertEquals(List.of(1, 2), ids);
        verify(questionDao, times(2)).insert(any(Question.class));
    }

    @Test
    void findExactQuestionShouldTrimInputsBeforeQueryingDao() {
        Question expected = validQuestion();
        when(questionDao.findByExactSignature("Java", QuestionType.SINGLE, "What is Java?", "A")).thenReturn(expected);

        Question actual = questionService.findExactQuestion(" Java ", QuestionType.SINGLE, " What is Java? ", " A ");

        assertSame(expected, actual);
        verify(questionDao).findByExactSignature("Java", QuestionType.SINGLE, "What is Java?", "A");
    }

    @Test
    void validateSupportedForAutoExamShouldRejectUnsupportedType() {
        Question question = validQuestion();
        question.setQuestionType(QuestionType.BLANK);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> questionService.validateSupportedForAutoExam(question)
        );

        assertTrue(exception.getMessage().contains("BLANK"));
    }

    private static Question validQuestion() {
        Question question = new Question();
        question.setQuestionType(QuestionType.SINGLE);
        question.setSubject("Java");
        question.setContent("What is Java?");
        question.setCorrectAnswer("A");
        question.setScore(5);
        question.setOptionA("A");
        question.setOptionB("B");
        return question;
    }
}
