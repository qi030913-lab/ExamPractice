package com.exam.model.enums;

/**
 * 题目类型枚举
 */
public enum QuestionType {
    SINGLE("单选题"),
    MULTIPLE("多选题"),
    JUDGE("判断题"),
    BLANK("填空题"),
    APPLICATION("应用题"),
    ALGORITHM("算法设计题");

    private final String description;

    QuestionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
