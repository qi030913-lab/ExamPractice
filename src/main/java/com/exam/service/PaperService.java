package com.exam.service;

import com.exam.dao.PaperDao;
import com.exam.dao.QuestionDao;
import com.exam.exception.BusinessException;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.enums.QuestionType;
import com.exam.util.DBUtil;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PaperService {
    private final PaperDao paperDao;
    private final QuestionDao questionDao;

    public PaperService(PaperDao paperDao, QuestionDao questionDao) {
        this.paperDao = paperDao;
        this.questionDao = questionDao;
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
            throw new BusinessException("创建试卷失败: " + e.getMessage(), e);
        }
    }

    public ImportPaperResult importPaper(Paper paper, List<Question> importedQuestions) {
        validatePaper(paper);

        if (importedQuestions == null || importedQuestions.isEmpty()) {
            throw new BusinessException("导入题目列表不能为空");
        }

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<Integer> linkedQuestionIds = new ArrayList<>();
                Set<Integer> uniqueQuestionIds = new LinkedHashSet<>();
                int createdQuestionCount = 0;
                int reusedQuestionCount = 0;
                int totalScore = 0;

                for (Question importedQuestion : importedQuestions) {
                    validateImportedQuestion(importedQuestion);

                    Question matched = questionDao.findByExactSignature(
                            conn,
                            importedQuestion.getSubject().trim(),
                            importedQuestion.getQuestionType(),
                            importedQuestion.getContent().trim(),
                            importedQuestion.getCorrectAnswer().trim()
                    );

                    Integer questionId;
                    Question resolvedQuestion;
                    if (matched == null) {
                        questionId = questionDao.insert(conn, importedQuestion);
                        importedQuestion.setQuestionId(questionId);
                        resolvedQuestion = importedQuestion;
                        createdQuestionCount++;
                    } else {
                        questionId = matched.getQuestionId();
                        resolvedQuestion = matched;
                        reusedQuestionCount++;
                    }

                    if (questionId != null && uniqueQuestionIds.add(questionId)) {
                        linkedQuestionIds.add(questionId);
                        totalScore += resolvedQuestion.getScore();
                    }
                }

                if (linkedQuestionIds.isEmpty()) {
                    throw new BusinessException("没有可用于建卷的有效题目");
                }

                paper.setTotalScore(totalScore);
                int paperId = paperDao.insert(conn, paper);
                paperDao.addPaperQuestionsBatch(conn, paperId, linkedQuestionIds);
                conn.commit();

                return new ImportPaperResult(
                        paperId,
                        importedQuestions.size(),
                        linkedQuestionIds.size(),
                        createdQuestionCount,
                        reusedQuestionCount
                );
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new BusinessException("导题建卷失败: " + e.getMessage(), e);
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

    private void validateImportedQuestion(Question question) {
        if (question == null) {
            throw new BusinessException("导入题目不能为空");
        }
        if (question.getQuestionType() == null) {
            throw new BusinessException("导入题目类型不能为空");
        }
        if (question.getSubject() == null || question.getSubject().trim().isEmpty()) {
            throw new BusinessException("导入题目科目不能为空");
        }
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            throw new BusinessException("导入题目内容不能为空");
        }
        if (question.getCorrectAnswer() == null || question.getCorrectAnswer().trim().isEmpty()) {
            throw new BusinessException("导入题目正确答案不能为空");
        }
        if (question.getScore() == null || question.getScore() <= 0) {
            throw new BusinessException("导入题目分值必须大于0");
        }
        if (question.getQuestionType() == QuestionType.SINGLE || question.getQuestionType() == QuestionType.MULTIPLE) {
            if (question.getOptionA() == null || question.getOptionA().trim().isEmpty()
                    || question.getOptionB() == null || question.getOptionB().trim().isEmpty()) {
                throw new BusinessException("选择题至少需要A、B两个选项");
            }
        }
        validateSupportedQuestion(question);
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
            String typeName = question.getQuestionType() == null ? "UNSPECIFIED" : question.getQuestionType().name();
            throw new BusinessException("当前考试流程仅支持 SINGLE、MULTIPLE、JUDGE 题型入卷，暂不支持题型: " + typeName);
        }
    }

    public PaperDao getPaperDao() {
        return paperDao;
    }

    public static class ImportPaperResult {
        private final int paperId;
        private final int sourceQuestionCount;
        private final int linkedQuestionCount;
        private final int createdQuestionCount;
        private final int reusedQuestionCount;

        public ImportPaperResult(
                int paperId,
                int sourceQuestionCount,
                int linkedQuestionCount,
                int createdQuestionCount,
                int reusedQuestionCount
        ) {
            this.paperId = paperId;
            this.sourceQuestionCount = sourceQuestionCount;
            this.linkedQuestionCount = linkedQuestionCount;
            this.createdQuestionCount = createdQuestionCount;
            this.reusedQuestionCount = reusedQuestionCount;
        }

        public int getPaperId() {
            return paperId;
        }

        public int getSourceQuestionCount() {
            return sourceQuestionCount;
        }

        public int getLinkedQuestionCount() {
            return linkedQuestionCount;
        }

        public int getCreatedQuestionCount() {
            return createdQuestionCount;
        }

        public int getReusedQuestionCount() {
            return reusedQuestionCount;
        }
    }
}
