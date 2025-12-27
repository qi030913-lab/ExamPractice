package com.exam.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 试卷实体类
 */
public class Paper {
    private Integer paperId;
    private String paperName;
    private String subject;
    private Integer totalScore;
    private Integer duration; // 考试时长（分钟）
    private Integer passScore;
    private String description;
    private Integer creatorId;
    private Boolean isPublished; // 是否发布，默认false
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 试卷包含的题目列表（体现组合关系）
    private List<Question> questions;

    public Paper() {
        this.questions = new ArrayList<>();
    }

    public Paper(String paperName, String subject, Integer duration, Integer passScore) {
        this.paperName = paperName;
        this.subject = subject;
        this.duration = duration;
        this.passScore = passScore;
        this.questions = new ArrayList<>();
    }

    // 添加题目
    public void addQuestion(Question question) {
        if (question != null) {
            this.questions.add(question);
        }
    }

    // 移除题目
    public void removeQuestion(Question question) {
        this.questions.remove(question);
    }

    // 计算总分
    public void calculateTotalScore() {
        this.totalScore = questions.stream()
                .mapToInt(Question::getScore)
                .sum();
    }

    // Getter和Setter方法
    public Integer getPaperId() {
        return paperId;
    }

    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

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

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getPassScore() {
        return passScore;
    }

    public void setPassScore(Integer passScore) {
        this.passScore = passScore;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paper paper = (Paper) o;
        return Objects.equals(paperId, paper.paperId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paperId);
    }

    @Override
    public String toString() {
        return "Paper{" +
                "paperId=" + paperId +
                ", paperName='" + paperName + '\'' +
                ", subject='" + subject + '\'' +
                ", totalScore=" + totalScore +
                ", duration=" + duration +
                ", passScore=" + passScore +
                '}';
    }
}
