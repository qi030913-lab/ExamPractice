package com.exam.model;

import com.exam.model.enums.ExamStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 考试记录实体类
 */
public class ExamRecord {
    private Integer recordId;
    private Integer studentId;
    private Integer paperId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime submitTime;
    private BigDecimal score;
    private ExamStatus status;
    private LocalDateTime createTime;
    
    // 学生答案（题目ID -> 学生答案）
    private Map<Integer, String> studentAnswers;
    
    // 关联对象
    private User student;
    private Paper paper;

    public ExamRecord() {
        this.status = ExamStatus.NOT_STARTED;
        this.studentAnswers = new HashMap<>();
    }

    public ExamRecord(Integer studentId, Integer paperId) {
        this.studentId = studentId;
        this.paperId = paperId;
        this.status = ExamStatus.NOT_STARTED;
        this.studentAnswers = new HashMap<>();
    }

    // 开始考试
    public void startExam() {
        this.startTime = LocalDateTime.now();
        this.status = ExamStatus.IN_PROGRESS;
    }

    // 提交考试
    public void submitExam() {
        this.submitTime = LocalDateTime.now();
        this.status = ExamStatus.SUBMITTED;
    }

    // 记录答案
    public void recordAnswer(Integer questionId, String answer) {
        this.studentAnswers.put(questionId, answer);
    }

    // Getter和Setter方法
    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getPaperId() {
        return paperId;
    }

    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public ExamStatus getStatus() {
        return status;
    }

    public void setStatus(ExamStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Map<Integer, String> getStudentAnswers() {
        return studentAnswers;
    }

    public void setStudentAnswers(Map<Integer, String> studentAnswers) {
        this.studentAnswers = studentAnswers;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Paper getPaper() {
        return paper;
    }

    public void setPaper(Paper paper) {
        this.paper = paper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExamRecord that = (ExamRecord) o;
        return Objects.equals(recordId, that.recordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId);
    }

    @Override
    public String toString() {
        return "ExamRecord{" +
                "recordId=" + recordId +
                ", studentId=" + studentId +
                ", paperId=" + paperId +
                ", score=" + score +
                ", status=" + status +
                '}';
    }
}
