package com.exam.model.enums;

/**
 * 难度等级枚举
 */
public enum Difficulty {
    EASY("简单"),
    MEDIUM("中等"),
    HARD("困难");

    private final String description;

    Difficulty(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
