package com.exam.service;

import com.exam.dao.PaperDao;
import com.exam.dao.QuestionDao;
import com.exam.exception.BusinessException;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.enums.QuestionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public int createPaper(Paper paper, List<Integer> questionIds) {
        validatePaper(paper);

        if (questionIds == null || questionIds.isEmpty()) {
            throw new BusinessException("Paper must include at least one question");
        }

        List<Integer> uniqueQuestionIds = new ArrayList<>(new LinkedHashSet<>(questionIds));
        Map<Integer, Question> questionMap = questionDao.findByIds(uniqueQuestionIds);

        int totalScore = 0;
        for (Integer questionId : uniqueQuestionIds) {
            if (questionId == null) {
                throw new BusinessException("Question ID cannot be null");
            }
            Question question = questionMap.get(questionId);
            if (question == null) {
                throw new BusinessException("Question ID " + questionId + " does not exist");
            }
            validateSupportedQuestion(question);
            totalScore += question.getScore();
        }
        paper.setTotalScore(totalScore);
        validatePassScoreWithinTotalScore(paper);

        int paperId = paperDao.insert(paper);
        paperDao.addPaperQuestionsBatch(paperId, uniqueQuestionIds);
        return paperId;
    }

    @Transactional
    public ImportPaperResult importPaper(Paper paper, List<Question> importedQuestions) {
        validatePaper(paper);

        if (importedQuestions == null || importedQuestions.isEmpty()) {
            throw new BusinessException("Imported question list cannot be empty");
        }

        List<Integer> linkedQuestionIds = new ArrayList<>();
        Set<Integer> uniqueQuestionIds = new LinkedHashSet<>();
        int createdQuestionCount = 0;
        int reusedQuestionCount = 0;
        int totalScore = 0;

        for (Question importedQuestion : importedQuestions) {
            validateImportedQuestion(importedQuestion);

            Question matched = questionDao.findByExactSignature(
                    importedQuestion.getSubject().trim(),
                    importedQuestion.getQuestionType(),
                    importedQuestion.getContent().trim(),
                    importedQuestion.getCorrectAnswer().trim()
            );

            Integer questionId;
            Question resolvedQuestion;
            if (matched == null) {
                questionId = questionDao.insert(importedQuestion);
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
            throw new BusinessException("No valid questions available for paper creation");
        }

        paper.setTotalScore(totalScore);
        validatePassScoreWithinTotalScore(paper);

        int paperId = paperDao.insert(paper);
        paperDao.addPaperQuestionsBatch(paperId, linkedQuestionIds);

        return new ImportPaperResult(
                paperId,
                importedQuestions.size(),
                linkedQuestionIds.size(),
                createdQuestionCount,
                reusedQuestionCount
        );
    }

    public int updatePaper(Paper paper) {
        if (paper.getPaperId() == null) {
            throw new BusinessException("Paper ID cannot be null");
        }
        validatePaper(paper);
        validatePassScoreWithinTotalScore(paper);
        return paperDao.update(paper);
    }

    public int deletePaper(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("Paper ID cannot be null");
        }
        return paperDao.delete(paperId);
    }

    public Paper getPaperById(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("Paper ID cannot be null");
        }

        Paper paper = paperDao.findById(paperId);
        if (paper == null) {
            throw new BusinessException("Paper does not exist");
        }

        List<Question> questions = questionDao.findByPaperId(paperId);
        paper.setQuestions(questions);

        return paper;
    }

    public Paper getPaperByName(String paperName) {
        if (paperName == null || paperName.trim().isEmpty()) {
            throw new BusinessException("Paper name cannot be blank");
        }

        Paper paper = paperDao.findByName(paperName);
        if (paper == null) {
            throw new BusinessException("Paper does not exist");
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
            throw new BusinessException("Paper ID cannot be null");
        }
        paperDao.updatePublishStatus(paperId, true);
    }

    public void unpublishPaper(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("Paper ID cannot be null");
        }
        paperDao.updatePublishStatus(paperId, false);
    }

    private void validateImportedQuestion(Question question) {
        if (question == null) {
            throw new BusinessException("Imported question cannot be null");
        }
        if (question.getQuestionType() == null) {
            throw new BusinessException("Imported question type cannot be null");
        }
        if (question.getSubject() == null || question.getSubject().trim().isEmpty()) {
            throw new BusinessException("Imported question subject cannot be blank");
        }
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            throw new BusinessException("Imported question content cannot be blank");
        }
        if (question.getCorrectAnswer() == null || question.getCorrectAnswer().trim().isEmpty()) {
            throw new BusinessException("Imported question answer cannot be blank");
        }
        if (question.getScore() == null || question.getScore() <= 0) {
            throw new BusinessException("Imported question score must be greater than 0");
        }
        if (question.getQuestionType() == QuestionType.SINGLE || question.getQuestionType() == QuestionType.MULTIPLE) {
            if (question.getOptionA() == null || question.getOptionA().trim().isEmpty()
                    || question.getOptionB() == null || question.getOptionB().trim().isEmpty()) {
                throw new BusinessException("Choice questions need at least option A and B");
            }
        }
        validateSupportedQuestion(question);
    }

    private void validatePaper(Paper paper) {
        if (paper == null) {
            throw new BusinessException("Paper payload cannot be null");
        }
        if (paper.getPaperName() == null || paper.getPaperName().trim().isEmpty()) {
            throw new BusinessException("Paper name cannot be blank");
        }
        if (paper.getSubject() == null || paper.getSubject().trim().isEmpty()) {
            throw new BusinessException("Subject cannot be blank");
        }
        if (paper.getDuration() == null || paper.getDuration() <= 0) {
            throw new BusinessException("Duration must be greater than 0");
        }
        if (paper.getPassScore() == null || paper.getPassScore() < 0) {
            throw new BusinessException("Pass score cannot be negative");
        }
    }

    private void validatePassScoreWithinTotalScore(Paper paper) {
        Integer totalScore = paper.getTotalScore();
        Integer passScore = paper.getPassScore();
        if (totalScore != null && passScore != null && passScore > totalScore) {
            throw new BusinessException("Pass score cannot exceed total score");
        }
    }

    private void validateSupportedQuestion(Question question) {
        if (question.getQuestionType() == null || !question.getQuestionType().isSupportedForAutoExam()) {
            String typeName = question.getQuestionType() == null ? "UNSPECIFIED" : question.getQuestionType().name();
            throw new BusinessException("当前考试流程仅支持 " + QuestionType.getAutoExamSupportedTypeNames() + " 题型，暂不支持题型：" + typeName);
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
