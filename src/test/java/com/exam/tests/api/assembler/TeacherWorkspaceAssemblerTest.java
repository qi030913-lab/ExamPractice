package com.exam.tests.api.assembler;

import com.exam.api.assembler.TeacherWorkspaceAssembler;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.User;
import com.exam.model.enums.ExamStatus;
import com.exam.model.enums.QuestionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeacherWorkspaceAssemblerTest {
    private final TeacherWorkspaceAssembler assembler = new TeacherWorkspaceAssembler();

    @Test
    void toTeacherStudentItemShouldIncludeSummaryFields() {
        User student = new User();
        student.setUserId(11);
        student.setRealName("张三");
        student.setLoginId("2024001");

        ExamRecord submitted = new ExamRecord(11, 101);
        submitted.setStatus(ExamStatus.SUBMITTED);
        submitted.setScore(new BigDecimal("80"));

        ExamRecord timeout = new ExamRecord(11, 102);
        timeout.setStatus(ExamStatus.TIMEOUT);
        timeout.setScore(new BigDecimal("60"));

        Map<String, Object> item = assembler.toTeacherStudentItem(student, List.of(submitted, timeout));

        assertEquals(11, item.get("userId"));
        assertEquals("张三", item.get("realName"));
        assertEquals(2, item.get("recordCount"));
        assertEquals(2L, item.get("submittedCount"));
        assertEquals(70.0d, (Double) item.get("averageScore"));
    }

    @Test
    void toTeacherPaperItemShouldPreferOptimizedQuestionCount() {
        Paper paper = new Paper();
        paper.setPaperId(1001);
        paper.setPaperName("Java测试");
        paper.setSingleCount(2);
        paper.setMultipleCount(1);
        paper.setJudgeCount(1);
        paper.setBlankCount(0);

        Map<String, Object> item = assembler.toTeacherPaperItem(paper);

        assertEquals(4, item.get("questionCount"));
    }

    @Test
    void toTeacherStudentRecordDetailItemShouldComputePassedFlag() {
        ExamRecord record = new ExamRecord(11, 101);
        record.setRecordId(5001);
        record.setScore(new BigDecimal("61"));
        record.setStartTime(LocalDateTime.now().minusMinutes(30));
        record.setSubmitTime(LocalDateTime.now());
        record.setStatus(ExamStatus.SUBMITTED);

        Paper paper = new Paper();
        paper.setPaperName("期中考试");
        paper.setPassScore(60);
        paper.setTotalScore(100);

        Question question = new Question();
        question.setQuestionId(1);
        question.setQuestionType(QuestionType.SINGLE);
        question.setContent("content");
        question.setCorrectAnswer("A");
        com.exam.model.AnswerRecord answer = new com.exam.model.AnswerRecord(5001, 1, "A");
        answer.setQuestion(question);

        Map<String, Object> item = assembler.toTeacherStudentRecordDetailItem(record, paper, List.of(answer), 1, 1, 0);

        assertEquals(1, item.get("questionCount"));
        assertEquals(1L, item.get("answeredCount"));
        assertTrue((Boolean) item.get("passed"));
    }
}
