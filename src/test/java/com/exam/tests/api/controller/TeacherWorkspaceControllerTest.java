package com.exam.tests.api.controller;

import com.exam.api.assembler.ExamRecordStatisticsAssembler;
import com.exam.api.assembler.TeacherWorkspaceAssembler;
import com.exam.api.common.ApiResponse;
import com.exam.api.controller.TeacherWorkspaceController;
import com.exam.api.dto.TeacherWorkspaceDtos;
import com.exam.api.support.ExamAccessGuard;
import com.exam.api.support.UserRoleGuard;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.User;
import com.exam.model.enums.ExamStatus;
import com.exam.model.enums.UserRole;
import com.exam.service.ExamService;
import com.exam.service.PaperService;
import com.exam.service.QuestionService;
import com.exam.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TeacherWorkspaceControllerTest {
    private UserService userService;
    private PaperService paperService;
    private ExamService examService;
    private QuestionService questionService;
    private UserRoleGuard userRoleGuard;
    private ExamAccessGuard examAccessGuard;
    private TeacherWorkspaceController controller;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        paperService = mock(PaperService.class);
        examService = mock(ExamService.class);
        questionService = mock(QuestionService.class);
        userRoleGuard = mock(UserRoleGuard.class);
        examAccessGuard = mock(ExamAccessGuard.class);
        controller = new TeacherWorkspaceController(
                userService,
                paperService,
                examService,
                questionService,
                new TeacherWorkspaceAssembler(new ExamRecordStatisticsAssembler()),
                userRoleGuard,
                examAccessGuard
        );
    }

    @Test
    void getTeacherStudentsShouldReturnTypedPayload() {
        User teacher = new User("Teacher", "teacher001", "secret", UserRole.TEACHER);
        teacher.setUserId(10);

        User studentA = new User("Alice", "2023001", "secret", UserRole.STUDENT);
        studentA.setUserId(1);
        User studentB = new User("Bob", "2023002", "secret", UserRole.STUDENT);
        studentB.setUserId(2);

        Paper paper = new Paper();
        paper.setPaperId(101);
        paper.setPaperName("Math Mock");

        ExamRecord submittedRecord = new ExamRecord();
        submittedRecord.setRecordId(11);
        submittedRecord.setStudentId(1);
        submittedRecord.setPaperId(101);
        submittedRecord.setPaper(paper);
        submittedRecord.setStatus(ExamStatus.SUBMITTED);
        submittedRecord.setScore(BigDecimal.valueOf(80));

        ExamRecord inProgressRecord = new ExamRecord();
        inProgressRecord.setRecordId(12);
        inProgressRecord.setStudentId(1);
        inProgressRecord.setPaperId(101);
        inProgressRecord.setPaper(paper);
        inProgressRecord.setStatus(ExamStatus.IN_PROGRESS);

        when(userRoleGuard.requireTeacher(10)).thenReturn(teacher);
        when(userService.getStudents()).thenReturn(List.of(studentA, studentB));
        when(examService.getStudentExamRecordsByStudentIds(List.of(1, 2))).thenReturn(Map.of(
                1, List.of(submittedRecord, inProgressRecord),
                2, List.of()
        ));

        ApiResponse<TeacherWorkspaceDtos.TeacherStudentsPayload> response = controller.getTeacherStudents(10);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(10, response.getData().user().getUserId());
        assertEquals(2, response.getData().summary().studentCount());
        assertEquals(2, response.getData().students().size());
        assertEquals(1, response.getData().students().get(0).userId());
        assertEquals(2, response.getData().students().get(0).recordCount());
        assertEquals(1L, response.getData().students().get(0).submittedCount());
        assertEquals(80.0, response.getData().students().get(0).averageScore(), 0.001);
        assertEquals(0, response.getData().students().get(1).recordCount());
    }
}
