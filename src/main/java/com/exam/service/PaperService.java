package com.exam.service;

import com.exam.dao.PaperDao;
import com.exam.dao.QuestionDao;
import com.exam.exception.BusinessException;
import com.exam.model.Paper;
import com.exam.model.Question;
import java.util.List;

/**
 * 试卷服务类
 */
public class PaperService {
    private final PaperDao paperDao;
    private final QuestionDao questionDao;

    public PaperService() {
        this.paperDao = new PaperDao();
        this.questionDao = new QuestionDao();
    }

    /**
     * 创建试卷（性能优化版本）
     * 使用批量查询获取题目信息，避免循环中单条查询
     */
    public int createPaper(Paper paper, List<Integer> questionIds) {
        validatePaper(paper);

        if (questionIds == null || questionIds.isEmpty()) {
            throw new BusinessException("试卷必须包含至少一道题目");
        }

        // 批量查询所有题目（性能优化：只查询1次数据库）
        java.util.Map<Integer, Question> questionMap = questionDao.findByIds(questionIds);
        
        // 验证题目是否存在并计算总分
        int totalScore = 0;
        for (Integer questionId : questionIds) {
            Question question = questionMap.get(questionId);
            if (question == null) {
                throw new BusinessException("题目ID " + questionId + " 不存在");
            }
            totalScore += question.getScore();
        }
        paper.setTotalScore(totalScore);

        // 保存试卷
        int paperId = paperDao.insert(paper);

        // 批量关联题目（性能优化：使用批量插入）
        paperDao.addPaperQuestionsBatch(paperId, questionIds);

        return paperId;
    }

    /**
     * 更新试卷基本信息
     */
    public int updatePaper(Paper paper) {
        if (paper.getPaperId() == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        validatePaper(paper);
        return paperDao.update(paper);
    }

    /**
     * 删除试卷
     */
    public int deletePaper(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        return paperDao.delete(paperId);
    }

    /**
     * 根据ID查询试卷（包含题目）
     */
    public Paper getPaperById(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }

        Paper paper = paperDao.findById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }

        // 加载试卷题目
        List<Question> questions = questionDao.findByPaperId(paperId);
        paper.setQuestions(questions);

        return paper;
    }

    /**
     * 根据试卷名称查询试卷（包含题目）
     */
    public Paper getPaperByName(String paperName) {
        if (paperName == null || paperName.trim().isEmpty()) {
            throw new BusinessException("试卷名称不能为空");
        }

        Paper paper = paperDao.findByName(paperName);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }

        // 加载试卷题目
        List<Question> questions = questionDao.findByPaperId(paper.getPaperId());
        paper.setQuestions(questions);

        return paper;
    }

    /**
     * 查询所有试卷（性能优化版本，用于教师端试卷列表展示）
     * 使用单条SQL查询试卷和题目数量，避免N+1问题
     * 题目数量存储在 singleCount 字段中
     * @return 试卷列表
     */
    public List<Paper> getAllPapersOptimized() {
        return paperDao.findAllWithQuestionCount();
    }

    /**
     * 查询所有试卷（性能优化版本）
     * 使用批量查询获取所有试卷的题目，避免N+1问题
     */
    public List<Paper> getAllPapers() {
        List<Paper> papers = paperDao.findAll();
        
        if (papers.isEmpty()) {
            return papers;
        }
        
        // 收集所有试卷ID
        List<Integer> paperIds = new java.util.ArrayList<>();
        for (Paper paper : papers) {
            paperIds.add(paper.getPaperId());
        }
        
        // 批量查询所有试卷的题目（性能优化：只查询1次数据库）
        java.util.Map<Integer, List<Question>> questionsMap = questionDao.findByPaperIds(paperIds);
        
        // 为每个试卷设置题目列表
        for (Paper paper : papers) {
            paper.setQuestions(questionsMap.getOrDefault(paper.getPaperId(), new java.util.ArrayList<>()));
        }
        
        return papers;
    }

    /**
     * 查询所有已发布的试卷（学生端使用）
     * 性能优化版本：使用批量查询获取题目，避免N+1问题
     */
    public List<Paper> getAllPublishedPapers() {
        List<Paper> papers = paperDao.findAllPublished();
        
        if (papers.isEmpty()) {
            return papers;
        }
        
        // 收集所有试卷ID
        List<Integer> paperIds = new java.util.ArrayList<>();
        for (Paper paper : papers) {
            paperIds.add(paper.getPaperId());
        }
        
        // 批量查询所有试卷的题目（性能优化：只查询1次数据库）
        java.util.Map<Integer, List<Question>> questionsMap = questionDao.findByPaperIds(paperIds);
        
        // 为每个试卷设置题目列表
        for (Paper paper : papers) {
            paper.setQuestions(questionsMap.getOrDefault(paper.getPaperId(), new java.util.ArrayList<>()));
        }
        
        return papers;
    }
    
    /**
     * 查询所有已发布的试卷及题型统计（性能优化版本）
     * 使用单条SQL查询，避免N+1问题
     * @return 试卷列表，每个试卷包含题型统计信息
     */
    public List<Paper> getAllPublishedPapersOptimized() {
        return paperDao.findAllPublishedWithQuestionStats();
    }

    /**
     * 发布试卷
     */
    public void publishPaper(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        paperDao.updatePublishStatus(paperId, true);
    }

    /**
     * 取消发布试卷
     */
    public void unpublishPaper(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        paperDao.updatePublishStatus(paperId, false);
    }

    /**
     * 验证试卷信息
     */
    private void validatePaper(Paper paper) {
        if (paper == null) {
            throw new BusinessException("试卷信息不能为空");
        }
        if (paper.getPaperName() == null || paper.getPaperName().trim().isEmpty()) {
            throw new BusinessException("试卷名称不能为空");
        }
        if (paper.getSubject() == null || paper.getSubject().trim().isEmpty()) {
            throw new BusinessException("科目不能为空");
        }
        if (paper.getDuration() == null || paper.getDuration() <= 0) {
            throw new BusinessException("考试时长必须大于0");
        }
        if (paper.getPassScore() == null || paper.getPassScore() < 0) {
            throw new BusinessException("及格分数不能为负数");
        }
    }
    
    /**
     * 获取PaperDao实例（用于性能优化查询）
     */
    public PaperDao getPaperDao() {
        return paperDao;
    }
}
