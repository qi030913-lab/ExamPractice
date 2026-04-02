package com.exam.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TeacherImportPaperRequest {
    @NotBlank(message = "试卷名称不能为空")
    private String paperName;

    @NotBlank(message = "科目不能为空")
    private String subject;

    @NotNull(message = "及格分不能为空")
    @Min(value = 0, message = "及格分不能小于0")
    private Integer passScore;

    @NotNull(message = "考试时长不能为空")
    @Min(value = 1, message = "考试时长必须大于0")
    private Integer duration;

    private String description;

    @NotBlank(message = "导题内容不能为空")
    private String sourceText;

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getPassScore() {
        return passScore;
    }

    public void setPassScore(Integer passScore) {
        this.passScore = passScore;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }
}
