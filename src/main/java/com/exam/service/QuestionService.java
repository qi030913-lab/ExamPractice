package com.exam.service;

import com.exam.dao.QuestionDao;
import com.exam.exception.BusinessException;
import com.exam.model.Question;
import java.util.List;
import java.util.ArrayList;

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
     * 批量添加题目
     */
    public List<Integer> batchAddQuestions(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new BusinessException("题目列表不能为空");
        }
        
        List<Integer> questionIds = new ArrayList<>();
        for (Question question : questions) {
            validateQuestion(question);
            int questionId = questionDao.insert(question);
            questionIds.add(questionId);
        }
        return questionIds;
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
    
    /**
     * 搜索题目
     * @param content 题目内容关键词
     * @param subject 科目
     * @param type 题目类型
     * @param difficulty 难度
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 题目列表
     */
    public List<Question> searchQuestions(String content, String subject, com.exam.model.enums.QuestionType type, com.exam.model.enums.Difficulty difficulty, int offset, int limit) {
        return questionDao.search(content, subject, type, difficulty, offset, limit);
    }
    
    /**
     * 统计符合条件的题目总数
     * @param content 题目内容关键词
     * @param subject 科目
     * @param type 题目类型
     * @param difficulty 难度
     * @return 题目总数
     */
    public int countQuestions(String content, String subject, com.exam.model.enums.QuestionType type, com.exam.model.enums.Difficulty difficulty) {
        return questionDao.countQuestions(content, subject, type, difficulty);
    }
}
