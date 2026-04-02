package com.exam.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StudentSubmitExamRequest {
    @NotNull(message = "答案列表不能为空")
    @Valid
    private List<AnswerItem> answers = new ArrayList<>();

    public List<AnswerItem> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerItem> answers) {
        this.answers = answers;
    }

    public static class AnswerItem {
        @NotNull(message = "题目 ID 不能为空")
        private Integer questionId;

        private String answer;

        public Integer getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Integer questionId) {
            this.questionId = questionId;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
}
