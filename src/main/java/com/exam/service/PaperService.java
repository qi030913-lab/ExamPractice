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
     * 创建试卷
     */
    public int createPaper(Paper paper, List<Integer> questionIds) {
        validatePaper(paper);

        if (questionIds == null || questionIds.isEmpty()) {
            throw new BusinessException("试卷必须包含至少一道题目");
        }

        // 计算总分
        int totalScore = 0;
        for (Integer questionId : questionIds) {
            Question question = questionDao.findById(questionId);
            if (question == null) {
                throw new BusinessException("题目ID " + questionId + " 不存在");
            }
            totalScore += question.getScore();
        }
        paper.setTotalScore(totalScore);

        // 保存试卷
        int paperId = paperDao.insert(paper);

        // 关联题目
        for (int i = 0; i < questionIds.size(); i++) {
            paperDao.addPaperQuestion(paperId, questionIds.get(i), i + 1);
        }

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
     * 查询所有试卷
     */
    public List<Paper> getAllPapers() {
        List<Paper> papers = paperDao.findAll();
        // 为每个试卷加载题目列表
        for (Paper paper : papers) {
            List<Question> questions = questionDao.findByPaperId(paper.getPaperId());
            paper.setQuestions(questions);
        }
        return papers;
    }

    /**
     * 查询所有已发布的试卷（学生端使用）
     */
    public List<Paper> getAllPublishedPapers() {
        List<Paper> papers = paperDao.findAllPublished();
        // 为每个试卷加载题目列表
        for (Paper paper : papers) {
            List<Question> questions = questionDao.findByPaperId(paper.getPaperId());
            paper.setQuestions(questions);
        }
        return papers;
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
}
