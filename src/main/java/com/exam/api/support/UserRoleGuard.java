package com.exam.api.support;

import com.exam.exception.BusinessException;
import com.exam.model.User;
import com.exam.model.enums.UserRole;
import com.exam.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class UserRoleGuard {
    private final UserService userService;

    public UserRoleGuard(UserService userService) {
        this.userService = userService;
    }

    public User requireTeacher(Integer userId) {
        return requireRole(userId, UserRole.TEACHER, "当前用户不是教师角色");
    }

    public User requireStudent(Integer userId) {
        return requireRole(userId, UserRole.STUDENT, "当前用户不是学生角色");
    }

    public User requireRole(Integer userId, UserRole expectedRole, String mismatchMessage) {
        User user = userService.getUserById(userId);
        if (user.getRole() != expectedRole) {
            throw new BusinessException(mismatchMessage);
        }
        return user;
    }
}
