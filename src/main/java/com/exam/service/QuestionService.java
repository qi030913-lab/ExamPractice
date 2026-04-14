package com.exam.service;

import com.exam.dao.QuestionDao;
import com.exam.exception.BusinessException;
import com.exam.model.Question;
import com.exam.model.enums.Difficulty;
import com.exam.model.enums.QuestionType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {
    private final QuestionDao questionDao;

    public QuestionService(QuestionDao questionDao) {
        this.questionDao = questionDao;
    }

    public int addQuestion(Question question) {
        validateQuestion(question);
        return questionDao.insert(question);
    }

    public int updateQuestion(Question question) {
        if (question.getQuestionId() == null) {
            throw new BusinessException("题目ID不能为空");
        }
        validateQuestion(question);
        return questionDao.update(question);
    }

    public int deleteQuestion(Integer questionId) {
        if (questionId == null) {
            throw new BusinessException("题目ID不能为空");
        }
        return questionDao.delete(questionId);
    }

    public Question getQuestionById(Integer questionId) {
        if (questionId == null) {
            throw new BusinessException("题目ID不能为空");
        }
        return questionDao.findById(questionId);
    }

    public List<Question> getAllQuestions() {
        return questionDao.findAll();
    }

    public List<Question> getQuestionsBySubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new BusinessException("科目不能为空");
        }
        return questionDao.findBySubject(subject);
    }

    public List<Question> getQuestionsByPaperId(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        return questionDao.findByPaperId(paperId);
    }

    public List<Integer> batchAddQuestions(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new BusinessException("题目列表不能为空");
        }

        List<Integer> questionIds = new ArrayList<>();
        for (Question question : questions) {
            validateQuestion(question);
            questionIds.add(questionDao.insert(question));
        }
        return questionIds;
    }

    public Question findExactQuestion(String subject, QuestionType type, String content, String correctAnswer) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new BusinessException("科目不能为空");
        }
        if (type == null) {
            throw new BusinessException("题目类型不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("题目内容不能为空");
        }
        if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
            throw new BusinessException("正确答案不能为空");
        }
        return questionDao.findByExactSignature(subject.trim(), type, content.trim(), correctAnswer.trim());
    }

    public boolean isSupportedForAutoExam(Question question) {
        return question != null
                && question.getQuestionType() != null
                && question.getQuestionType().isSupportedForAutoExam();
    }

    public void validateSupportedForAutoExam(Question question) {
        if (!isSupportedForAutoExam(question)) {
            String typeName = question == null || question.getQuestionType() == null
                    ? "UNSPECIFIED"
                    : question.getQuestionType().name();
            throw new BusinessException("当前考试流程仅支持 " + QuestionType.getAutoExamSupportedTypeNames() + " 题型，暂不支持题型：" + typeName);
        }
    }

    private void validateQuestion(Question question) {
        if (question == null) {
            throw new BusinessException("题目信息不能为空");
        }
        if (question.getQuestionType() == null) {
            throw new BusinessException("题目类型不能为空");
        }
        if (question.getSubject() == null || question.getSubject().trim().isEmpty()) {
            throw new BusinessException("科目不能为空");
        }
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            throw new BusinessException("题目内容不能为空");
        }
        if (question.getCorrectAnswer() == null || question.getCorrectAnswer().trim().isEmpty()) {
            throw new BusinessException("正确答案不能为空");
        }
        if (question.getScore() == null || question.getScore() <= 0) {
            throw new BusinessException("题目分值必须大于0");
        }

        switch (question.getQuestionType()) {
            case SINGLE:
            case MULTIPLE:
                if (question.getOptionA() == null || question.getOptionA().trim().isEmpty()
                        || question.getOptionB() == null || question.getOptionB().trim().isEmpty()) {
                    throw new BusinessException("选择题至少需要A、B两个选项");
                }
                break;
            case JUDGE:
                break;
            default:
                break;
        }
    }

    public List<Question> searchQuestions(
            String content,
            String subject,
            QuestionType type,
            Difficulty difficulty,
            int offset,
            int limit
    ) {
        return questionDao.search(content, subject, type, difficulty, offset, limit);
    }

    public int countQuestions(String content, String subject, QuestionType type, Difficulty difficulty) {
        return questionDao.countQuestions(content, subject, type, difficulty);
    }
}
