package com.exam.model.enums;

/**
 * 考试状态枚举
 */
public enum ExamStatus {
    NOT_STARTED("未开始"),
    IN_PROGRESS("进行中"),
    SUBMITTED("已提交"),
    TIMEOUT("已超时");

    private final String description;

    ExamStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
