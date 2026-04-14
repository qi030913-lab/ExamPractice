package com.exam.tests.api.controller;

import com.exam.api.assembler.ExamRecordStatisticsAssembler;
import com.exam.api.assembler.StudentWorkspaceAssembler;
import com.exam.api.common.ApiResponse;
import com.exam.api.controller.WorkbenchController;
import com.exam.api.dto.WorkbenchDtos;
import com.exam.api.support.UserRoleGuard;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.User;
import com.exam.model.enums.ExamStatus;
import com.exam.model.enums.UserRole;
import com.exam.service.ExamService;
import com.exam.service.PaperService;
import com.exam.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkbenchControllerTest {
    private UserService userService;
    private PaperService paperService;
    private ExamService examService;
    private UserRoleGuard userRoleGuard;
    private WorkbenchController controller;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        paperService = mock(PaperService.class);
        examService = mock(ExamService.class);
        userRoleGuard = mock(UserRoleGuard.class);
        controller = new WorkbenchController(
                userService,
                paperService,
                examService,
                new StudentWorkspaceAssembler(new ExamRecordStatisticsAssembler()),
                userRoleGuard
        );
    }

    @Test
    void studentWorkbenchShouldReturnTypedPayload() {
        User student = buildUser(1, "Alice", UserRole.STUDENT);
        Paper publishedPaper = buildPaper(101, "Math Mock", 100, 60);
        Paper ongoingPaper = buildPaper(102, "English Mock", 100, 60);

        ExamRecord submittedRecord = buildRecord(11, student.getUserId(), publishedPaper, ExamStatus.SUBMITTED, BigDecimal.valueOf(88));
        submittedRecord.setStartTime(LocalDateTime.of(2026, 4, 10, 9, 0));
        submittedRecord.setSubmitTime(LocalDateTime.of(2026, 4, 10, 9, 45));

        ExamRecord ongoingRecord = buildRecord(12, student.getUserId(), ongoingPaper, ExamStatus.IN_PROGRESS, null);
        ongoingRecord.setStartTime(LocalDateTime.of(2026, 4, 13, 10, 0));

        when(userRoleGuard.requireStudent(1)).thenReturn(student);
        when(paperService.getAllPublishedPapersOptimized()).thenReturn(List.of(publishedPaper, ongoingPaper));
        when(examService.getStudentExamRecordsOptimized(1)).thenReturn(List.of(submittedRecord, ongoingRecord));

        ApiResponse<WorkbenchDtos.StudentWorkbenchPayload> response = controller.studentWorkbench(1);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().user().getUserId());
        assertEquals(2, response.getData().stats().publishedPaperCount());
        assertEquals(2, response.getData().stats().recordCount());
        assertEquals(1L, response.getData().stats().submittedCount());
        assertEquals(88.0, response.getData().stats().averageScore(), 0.001);
        assertNotNull(response.getData().ongoingRecord());
        assertEquals(12, response.getData().ongoingRecord().recordId());
        assertEquals("English Mock", response.getData().ongoingRecord().paperName());
        assertTrue(response.getData().ongoingRecord().resumeAvailable());
    }

    private User buildUser(int userId, String realName, UserRole role) {
        User user = new User(realName, realName.toLowerCase(), "secret", role);
        user.setUserId(userId);
        return user;
    }

    private Paper buildPaper(int paperId, String paperName, int totalScore, int passScore) {
        Paper paper = new Paper();
        paper.setPaperId(paperId);
        paper.setPaperName(paperName);
        paper.setSubject("Subject");
        paper.setTotalScore(totalScore);
        paper.setPassScore(passScore);
        paper.setDuration(60);
        paper.setIsPublished(true);
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
}
