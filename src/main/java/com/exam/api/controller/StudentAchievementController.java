package com.exam.api.controller;

import com.exam.api.assembler.StudentAchievementAssembler;
import com.exam.api.common.ApiResponse;
import com.exam.api.dto.AuthUserResponse;
import com.exam.api.service.StudentAchievementService;
import com.exam.exception.BusinessException;
import com.exam.model.User;
import com.exam.model.enums.UserRole;
import com.exam.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentAchievementController {
    private final UserService userService;
    private final StudentAchievementService studentAchievementService;
    private final StudentAchievementAssembler assembler;

    public StudentAchievementController(
            UserService userService,
            StudentAchievementService studentAchievementService,
            StudentAchievementAssembler assembler
    ) {
        this.userService = userService;
        this.studentAchievementService = studentAchievementService;
        this.assembler = assembler;
    }

    @GetMapping("/{userId}/achievement")
    public ApiResponse<Map<String, Object>> getStudentAchievement(@PathVariable("userId") Integer userId) {
        User student = requireStudent(userId);
        StudentAchievementService.StudentAchievementSnapshot snapshot = studentAchievementService.buildSnapshot(userId);

        Map<String, Object> payload = assembler.toPayload(snapshot);
        payload.put("user", AuthUserResponse.from(student));
        return ApiResponse.success("学生成绩数据加载成功", payload);
    }

    private User requireStudent(Integer userId) {
        User user = userService.getUserById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException("当前用户不是学生角色");
        }
        return user;
    }
}
