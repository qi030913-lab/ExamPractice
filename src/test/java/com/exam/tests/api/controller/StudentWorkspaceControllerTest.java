package com.exam.tests.api.controller;

import com.exam.api.assembler.ExamRecordStatisticsAssembler;
import com.exam.api.assembler.StudentWorkspaceAssembler;
import com.exam.api.common.ApiResponse;
import com.exam.api.controller.StudentWorkspaceController;
import com.exam.api.dto.StudentWorkspaceDtos;
import com.exam.api.support.ExamAccessGuard;
import com.exam.api.support.UserRoleGuard;
import com.exam.exception.BusinessException;
import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.User;
import com.exam.model.enums.ExamStatus;
import com.exam.model.enums.QuestionType;
import com.exam.model.enums.UserRole;
import com.exam.service.ExamService;
import com.exam.service.PaperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentWorkspaceControllerTest {
    private PaperService paperService;
    private ExamService examService;
    private UserRoleGuard userRoleGuard;
    private ExamAccessGuard examAccessGuard;
    private StudentWorkspaceController controller;

    @BeforeEach
    void setUp() {
        paperService = mock(PaperService.class);
        examService = mock(ExamService.class);
        userRoleGuard = mock(UserRoleGuard.class);
        examAccessGuard = mock(ExamAccessGuard.class);
        controller = new StudentWorkspaceController(
                paperService,
                examService,
                new StudentWorkspaceAssembler(new ExamRecordStatisticsAssembler()),
                userRoleGuard,
                examAccessGuard
        );
    }

    @Test
    void getStudentRecordsShouldReturnTypedPayload() {
        User student = new User("Alice", "2023001", "secret", UserRole.STUDENT);
        student.setUserId(1);

        Paper mathPaper = buildPaper(101, "Math Mock");
        Paper englishPaper = buildPaper(102, "English Mock");

        ExamRecord mathRecord = buildRecord(11, 1, mathPaper, ExamStatus.SUBMITTED, BigDecimal.valueOf(95));
        mathRecord.setStartTime(LocalDateTime.of(2026, 4, 10, 9, 0));
        mathRecord.setSubmitTime(LocalDateTime.of(2026, 4, 10, 9, 30));

        ExamRecord englishRecord = buildRecord(12, 1, englishPaper, ExamStatus.TIMEOUT, BigDecimal.valueOf(40));
        englishRecord.setStartTime(LocalDateTime.of(2026, 4, 11, 9, 0));
        englishRecord.setEndTime(LocalDateTime.of(2026, 4, 11, 10, 0));

        when(userRoleGuard.requireStudent(1)).thenReturn(student);
        when(examService.getStudentExamRecordsOptimized(1)).thenReturn(List.of(mathRecord, englishRecord));
        when(examService.getAnswerRecordsBatch(List.of(11, 12))).thenReturn(Map.of(
                11, List.of(
                        buildAnswerRecord(11, 1001, "A", true),
                        buildAnswerRecord(11, 1002, "B", false)
                ),
                12, List.of(
                        buildAnswerRecord(12, 1003, "", false)
                )
        ));

        ApiResponse<StudentWorkspaceDtos.StudentRecordsPayload> response = controller.getStudentRecords(1);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().user().getUserId());
        assertEquals(2, response.getData().summary().recordCount());
        assertEquals(2L, response.getData().summary().submittedCount());
        assertEquals(67.5, response.getData().summary().averageScore(), 0.001);
        assertEquals(2, response.getData().records().size());
        assertEquals(11, response.getData().records().get(0).recordId());
        assertEquals("Math Mock", response.getData().records().get(0).paperName());
        assertEquals(1L, response.getData().records().get(0).correctCount());
        assertEquals(1L, response.getData().records().get(0).wrongCount());
    }

    @Test
    void startExamShouldValidateUnsupportedQuestionTypesBeforeCreatingRecord() {
        User student = new User("Alice", "2023001", "secret", UserRole.STUDENT);
        student.setUserId(1);
        Paper paper = buildPaper(101, "Legacy Paper");

        when(userRoleGuard.requireStudent(1)).thenReturn(student);
        when(examAccessGuard.requirePublishedPaper(101)).thenReturn(paper);
        doThrow(new BusinessException("当前考试流程仅支持 SINGLE, MULTIPLE, JUDGE 题型，当前试卷包含不支持自动判分的题型：SHORT_ANSWER。请联系老师重新发布仅含客观题的试卷。"))
                .when(examService).validatePaperSupportsAutoExam(101);

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.startExam(1, 101));

        assertTrue(exception.getMessage().contains("SHORT_ANSWER"));
        verify(examService, never()).startOrResumeExam(1, 101);
    }

    private Paper buildPaper(int paperId, String paperName) {
        Paper paper = new Paper();
        paper.setPaperId(paperId);
        paper.setPaperName(paperName);
        paper.setSubject("Subject");
        paper.setTotalScore(100);
        paper.setPassScore(60);
        return paper;
    }

    private ExamRecord buildRecord(Integer recordId, Integer studentId, Paper paper, ExamStatus status, BigDecimal score) {
        ExamRecord record = new ExamRecord();
        record.setRecordId(recordId);
        record.setStudentId(studentId);
        record.setPaperId(paper.getPaperId());
        record.setPaper(paper);
        record.setStatus(status);
        record.setScore(score);
        return record;
    }

    private AnswerRecord buildAnswerRecord(Integer recordId, Integer questionId, String answer, boolean correct) {
        AnswerRecord answerRecord = new AnswerRecord(recordId, questionId, answer);
        answerRecord.setIsCorrect(correct);
        return answerRecord;
    }
}
