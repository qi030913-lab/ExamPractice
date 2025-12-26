package com.exam.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 答题记录实体类
 */
public class AnswerRecord {
    private Integer answerId;
    private Integer recordId;
    private Integer questionId;
    private String studentAnswer;
    private Boolean isCorrect;
    private BigDecimal score;
    
    // 关联对象
    private Question question;

    public AnswerRecord() {
    }

    public AnswerRecord(Integer recordId, Integer questionId, String studentAnswer) {
        this.recordId = recordId;
        this.questionId = questionId;
        this.studentAnswer = studentAnswer;
    }

    // Getter和Setter方法
    public Integer getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Integer answerId) {
        this.answerId = answerId;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getStudentAnswer() {
        return studentAnswer;
    }

    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswerRecord that = (AnswerRecord) o;
        return Objects.equals(answerId, that.answerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(answerId);
    }

    @Override
    public String toString() {
        return "AnswerRecord{" +
                "answerId=" + answerId +
                ", recordId=" + recordId +
                ", questionId=" + questionId +
                ", studentAnswer='" + studentAnswer + '\'' +
                ", isCorrect=" + isCorrect +
                ", score=" + score +
                '}';
    }
}
