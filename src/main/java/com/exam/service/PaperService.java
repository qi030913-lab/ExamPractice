package com.exam.service;

import com.exam.dao.PaperDao;
import com.exam.dao.QuestionDao;
import com.exam.exception.BusinessException;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.util.DBUtil;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class PaperService {
    private final PaperDao paperDao;
    private final QuestionDao questionDao;

    public PaperService() {
        this.paperDao = new PaperDao();
        this.questionDao = new QuestionDao();
    }

    public int createPaper(Paper paper, List<Integer> questionIds) {
        validatePaper(paper);

        if (questionIds == null || questionIds.isEmpty()) {
            throw new BusinessException("试卷必须至少包含一道题");
        }

        List<Integer> uniqueQuestionIds = new ArrayList<>(new LinkedHashSet<>(questionIds));
        Map<Integer, Question> questionMap = questionDao.findByIds(uniqueQuestionIds);

        int totalScore = 0;
        for (Integer questionId : uniqueQuestionIds) {
            if (questionId == null) {
                throw new BusinessException("题目ID不能为空");
            }
            Question question = questionMap.get(questionId);
            if (question == null) {
                throw new BusinessException("题目 ID " + questionId + " 不存在");
            }
            validateSupportedQuestion(question);
            totalScore += question.getScore();
        }
        paper.setTotalScore(totalScore);

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int paperId = paperDao.insert(conn, paper);
                paperDao.addPaperQuestionsBatch(conn, paperId, uniqueQuestionIds);
                conn.commit();
                return paperId;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new BusinessException("创建试卷失败：" + e.getMessage());
        }
    }

    public int updatePaper(Paper paper) {
        if (paper.getPaperId() == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        validatePaper(paper);
        return paperDao.update(paper);
    }

    public int deletePaper(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        return paperDao.delete(paperId);
    }

    public Paper getPaperById(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }

        Paper paper = paperDao.findById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }

        List<Question> questions = questionDao.findByPaperId(paperId);
        paper.setQuestions(questions);

        return paper;
    }

    public Paper getPaperByName(String paperName) {
        if (paperName == null || paperName.trim().isEmpty()) {
            throw new BusinessException("试卷名称不能为空");
        }

        Paper paper = paperDao.findByName(paperName);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }

        List<Question> questions = questionDao.findByPaperId(paper.getPaperId());
        paper.setQuestions(questions);

        return paper;
    }

    public List<Paper> getAllPapersOptimized() {
        return paperDao.findAllWithQuestionCount();
    }

    public List<Paper> getAllPapers() {
        List<Paper> papers = paperDao.findAll();

        if (papers.isEmpty()) {
            return papers;
        }

        List<Integer> paperIds = new ArrayList<>();
        for (Paper paper : papers) {
            paperIds.add(paper.getPaperId());
        }

        Map<Integer, List<Question>> questionsMap = questionDao.findByPaperIds(paperIds);

        for (Paper paper : papers) {
            paper.setQuestions(questionsMap.getOrDefault(paper.getPaperId(), new ArrayList<>()));
        }

        return papers;
    }

    public List<Paper> getAllPublishedPapers() {
        List<Paper> papers = paperDao.findAllPublished();

        if (papers.isEmpty()) {
            return papers;
        }

        List<Integer> paperIds = new ArrayList<>();
        for (Paper paper : papers) {
            paperIds.add(paper.getPaperId());
        }

        Map<Integer, List<Question>> questionsMap = questionDao.findByPaperIds(paperIds);

        for (Paper paper : papers) {
            paper.setQuestions(questionsMap.getOrDefault(paper.getPaperId(), new ArrayList<>()));
        }

        return papers;
    }

    public List<Paper> getAllPublishedPapersOptimized() {
        return paperDao.findAllPublishedWithQuestionStats();
    }

    public void publishPaper(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        paperDao.updatePublishStatus(paperId, true);
    }

    public void unpublishPaper(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        paperDao.updatePublishStatus(paperId, false);
    }

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

    private void validateSupportedQuestion(Question question) {
        if (question.getQuestionType() == null || !question.getQuestionType().isSupportedForAutoExam()) {
            String typeName = question.getQuestionType() == null ? "未指定" : question.getQuestionType().name();
            throw new BusinessException("当前考试流程仅支持 SINGLE、MULTIPLE、JUDGE 题型入卷，暂不支持题型：" + typeName);
        }
    }

    public PaperDao getPaperDao() {
        return paperDao;
    }
}
