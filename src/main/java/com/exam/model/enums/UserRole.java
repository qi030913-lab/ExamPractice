package com.exam.model.enums;

import java.util.Locale;

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

    public static UserRole fromCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("角色不能为空");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (UserRole role : values()) {
            if (role.name().equals(normalized)) {
                return role;
            }
        }

        throw new IllegalArgumentException("角色仅支持 STUDENT 或 TEACHER");
    }
}
