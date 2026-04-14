package com.exam.tests.api.controller;

import com.exam.api.assembler.StudentAchievementAssembler;
import com.exam.api.common.ApiResponse;
import com.exam.api.controller.StudentAchievementController;
import com.exam.api.dto.StudentAchievementDtos;
import com.exam.api.service.StudentAchievementService;
import com.exam.api.support.UserRoleGuard;
import com.exam.model.User;
import com.exam.model.enums.UserRole;
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

class StudentAchievementControllerTest {
    private StudentAchievementService studentAchievementService;
    private UserRoleGuard userRoleGuard;
    private StudentAchievementController controller;

    @BeforeEach
    void setUp() {
        studentAchievementService = mock(StudentAchievementService.class);
        userRoleGuard = mock(UserRoleGuard.class);
        controller = new StudentAchievementController(
                studentAchievementService,
                new StudentAchievementAssembler(),
                userRoleGuard
        );
    }

    @Test
    void getStudentAchievementShouldReturnTypedPayload() {
        User student = new User("Alice", "2023001", "secret", UserRole.STUDENT);
        student.setUserId(1);

        LocalDateTime updatedAt = LocalDateTime.of(2026, 4, 13, 16, 0);
        StudentAchievementService.StudentAchievementSnapshot snapshot = new StudentAchievementService.StudentAchievementSnapshot(
                new StudentAchievementService.AchievementSummary(
                        3,
                        2,
                        1,
                        81.5,
                        BigDecimal.valueOf(93),
                        1,
                        1,
                        20,
                        18,
                        15,
                        75.0
                ),
                List.of(new StudentAchievementService.ScoreTrendItem(
                        11,
                        101,
                        "Math Mock",
                        "Math",
                        BigDecimal.valueOf(93),
                        100,
                        60,
                        "SUBMITTED",
                        updatedAt.minusDays(1),
                        updatedAt.minusDays(1).minusHours(1),
                        true
                )),
                List.of(new StudentAchievementService.TypeAccuracyItem(
                        "SINGLE",
                        "single",
                        10,
                        9,
                        8,
                        80.0
                )),
                List.of(new StudentAchievementService.SubjectPerformanceItem(
                        "Math",
                        2,
                        81.5,
                        1,
                        1,
                        20,
                        18,
                        15,
                        75.0,
                        updatedAt.minusDays(1)
                )),
                updatedAt
        );

        when(userRoleGuard.requireStudent(1)).thenReturn(student);
        when(studentAchievementService.buildSnapshot(1)).thenReturn(snapshot);

        ApiResponse<StudentAchievementDtos.StudentAchievementPayload> response = controller.getStudentAchievement(1);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().user().getUserId());
        assertEquals(3, response.getData().summary().totalExamCount());
        assertEquals(2, response.getData().summary().completedExamCount());
        assertEquals(81.5, response.getData().summary().averageScore(), 0.001);
        assertEquals(BigDecimal.valueOf(93), response.getData().summary().bestScore());
        assertEquals(1, response.getData().scoreTrend().size());
        assertEquals("Math Mock", response.getData().scoreTrend().get(0).paperName());
        assertEquals(1, response.getData().questionTypeAccuracy().size());
        assertEquals("SINGLE", response.getData().questionTypeAccuracy().get(0).questionType());
        assertEquals(updatedAt, response.getData().latestUpdatedAt());
    }
}
