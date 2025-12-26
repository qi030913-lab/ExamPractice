package com.exam.model.enums;

/**
 * 用户角色枚举
 */
public enum UserRole {
    STUDENT("学生"),
    TEACHER("教师");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
