package com.exam.api.controller;

import com.exam.api.assembler.StudentAchievementAssembler;
import com.exam.api.common.ApiResponse;
import com.exam.api.dto.AuthUserResponse;
import com.exam.api.dto.StudentAchievementDtos;
import com.exam.api.service.StudentAchievementService;
import com.exam.api.support.UserRoleGuard;
import com.exam.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentAchievementController {
    private final StudentAchievementService studentAchievementService;
    private final StudentAchievementAssembler assembler;
    private final UserRoleGuard userRoleGuard;

    public StudentAchievementController(
            StudentAchievementService studentAchievementService,
            StudentAchievementAssembler assembler,
            UserRoleGuard userRoleGuard
    ) {
        this.studentAchievementService = studentAchievementService;
        this.assembler = assembler;
        this.userRoleGuard = userRoleGuard;
    }

    @GetMapping("/{userId}/achievement")
    public ApiResponse<StudentAchievementDtos.StudentAchievementPayload> getStudentAchievement(@PathVariable("userId") Integer userId) {
        User student = userRoleGuard.requireStudent(userId);
        StudentAchievementService.StudentAchievementSnapshot snapshot = studentAchievementService.buildSnapshot(userId);
        return ApiResponse.success(
                "学生成绩数据加载成功",
                assembler.toPayload(AuthUserResponse.from(student), snapshot)
        );
    }
}
