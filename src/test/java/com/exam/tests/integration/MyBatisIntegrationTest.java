package com.exam.tests.integration;

import com.exam.api.ApiApplication;
import com.exam.config.MyBatisConfig;
import com.exam.dao.PaperDao;
import com.exam.dao.QuestionDao;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.enums.QuestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MybatisTest
@ContextConfiguration(classes = {ApiApplication.class, MyBatisConfig.class})
class MyBatisIntegrationTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private PaperDao paperDao;

    @BeforeEach
    void setUpSchema() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS paper_question");
            statement.execute("DROP TABLE IF EXISTS question");
            statement.execute("DROP TABLE IF EXISTS paper");

            statement.execute("""
                    CREATE TABLE paper (
                        paper_id INT PRIMARY KEY AUTO_INCREMENT,
                        paper_name VARCHAR(100) NOT NULL,
                        subject VARCHAR(50) NOT NULL,
                        total_score INT DEFAULT 100,
                        duration INT DEFAULT 90,
                        pass_score INT DEFAULT 60,
                        description TEXT,
                        is_published BOOLEAN DEFAULT FALSE,
                        creator_id INT,
                        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            statement.execute("""
                    CREATE TABLE question (
                        question_id INT PRIMARY KEY AUTO_INCREMENT,
                        question_type VARCHAR(32) NOT NULL,
                        subject VARCHAR(50) NOT NULL,
                        content TEXT NOT NULL,
                        option_a VARCHAR(500),
                        option_b VARCHAR(500),
                        option_c VARCHAR(500),
                        option_d VARCHAR(500),
                        correct_answer TEXT NOT NULL,
                        score INT DEFAULT 5,
                        difficulty VARCHAR(32) DEFAULT 'MEDIUM',
                        analysis TEXT,
                        creator_id INT,
                        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            statement.execute("""
                    CREATE TABLE paper_question (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        paper_id INT NOT NULL,
                        question_id INT NOT NULL,
                        question_order INT NOT NULL
                    )
                    """);
        }
    }

    @Test
    void questionMapperShouldInsertAndQueryByIds() {
        Question question = new Question();
        question.setQuestionType(QuestionType.SINGLE);
        question.setSubject("Java");
        question.setContent("What is JVM?");
        question.setOptionA("A");
        question.setOptionB("B");
        question.setCorrectAnswer("A");
        question.setScore(5);

        int rows = questionDao.insert(question);

        assertEquals(1, rows);
        assertNotNull(question.getQuestionId());

        Map<Integer, Question> result = questionDao.findByIds(List.of(question.getQuestionId()));
        assertEquals(1, result.size());
        assertEquals("What is JVM?", result.get(question.getQuestionId()).getContent());
    }

    @Test
    void paperMapperShouldInsertAndCountQuestions() {
        Paper paper = new Paper();
        paper.setPaperName("Java Basics");
        paper.setSubject("Java");
        paper.setTotalScore(10);
        paper.setDuration(60);
        paper.setPassScore(6);
        paper.setDescription("test");

        Question q1 = new Question();
        q1.setQuestionType(QuestionType.SINGLE);
        q1.setSubject("Java");
        q1.setContent("Q1");
        q1.setOptionA("A");
        q1.setOptionB("B");
        q1.setCorrectAnswer("A");
        q1.setScore(5);

        Question q2 = new Question();
        q2.setQuestionType(QuestionType.JUDGE);
        q2.setSubject("Java");
        q2.setContent("Q2");
        q2.setCorrectAnswer("T");
        q2.setScore(5);

        questionDao.insert(q1);
        questionDao.insert(q2);
        paperDao.insert(paper);
        paperDao.addPaperQuestionsBatch(paper.getPaperId(), List.of(q1.getQuestionId(), q2.getQuestionId()));

        List<Paper> papers = paperDao.findAllWithQuestionCount();

        assertEquals(1, papers.size());
        assertTrue(papers.get(0).getSingleCount() >= 2);
    }
}
