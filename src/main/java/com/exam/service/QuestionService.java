package com.exam.service;

import com.exam.dao.QuestionDao;
import com.exam.exception.BusinessException;
import com.exam.model.Question;
import java.util.List;

/**
 * 题目服务类
 */
public class QuestionService {
    private final QuestionDao questionDao;

    public QuestionService() {
        this.questionDao = new QuestionDao();
    }

    /**
     * 添加题目
     */
    public int addQuestion(Question question) {
        validateQuestion(question);
        return questionDao.insert(question);
    }

    /**
     * 更新题目
     */
    public int updateQuestion(Question question) {
        if (question.getQuestionId() == null) {
            throw new BusinessException("题目ID不能为空");
        }
        validateQuestion(question);
        return questionDao.update(question);
    }

    /**
     * 删除题目
     */
    public int deleteQuestion(Integer questionId) {
        if (questionId == null) {
            throw new BusinessException("题目ID不能为空");
        }
        return questionDao.delete(questionId);
    }

    /**
     * 根据ID查询题目
     */
    public Question getQuestionById(Integer questionId) {
        if (questionId == null) {
            throw new BusinessException("题目ID不能为空");
        }
        return questionDao.findById(questionId);
    }

    /**
     * 查询所有题目
     */
    public List<Question> getAllQuestions() {
        return questionDao.findAll();
    }

    /**
     * 根据科目查询题目
     */
    public List<Question> getQuestionsBySubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new BusinessException("科目不能为空");
        }
        return questionDao.findBySubject(subject);
    }

    /**
     * 根据试卷ID查询题目
     */
    public List<Question> getQuestionsByPaperId(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        return questionDao.findByPaperId(paperId);
    }

    /**
     * 验证题目信息
     */
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

        // 根据题目类型验证选项
        switch (question.getQuestionType()) {
            case SINGLE:
            case MULTIPLE:
                if (question.getOptionA() == null || question.getOptionA().trim().isEmpty() ||
                    question.getOptionB() == null || question.getOptionB().trim().isEmpty()) {
                    throw new BusinessException("选择题至少需要A、B两个选项");
                }
                break;
            case JUDGE:
                // 判断题不需要选项
                break;
        }
    }
}
