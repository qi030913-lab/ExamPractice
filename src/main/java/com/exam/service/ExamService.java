package com.exam.service;

import com.exam.dao.ExamRecordDao;
import com.exam.dao.PaperDao;
import com.exam.dao.QuestionDao;
import com.exam.exception.BusinessException;
import com.exam.model.*;
import com.exam.model.enums.ExamStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 考试服务类
 * 处理考试相关的业务逻辑，包括自动判分
 */
public class ExamService {
    private final ExamRecordDao examRecordDao;
    private final PaperDao paperDao;
    private final QuestionDao questionDao;

    public ExamService() {
        this.examRecordDao = new ExamRecordDao();
        this.paperDao = new PaperDao();
        this.questionDao = new QuestionDao();
    }

    /**
     * 开始考试
     */
    public ExamRecord startExam(Integer studentId, Integer paperId) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }

        // 检查试卷是否存在
        Paper paper = paperDao.findById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }

        // 创建考试记录
        ExamRecord record = new ExamRecord(studentId, paperId);
        record.startExam();
        
        int recordId = examRecordDao.insert(record);
        record.setRecordId(recordId);

        return record;
    }

    /**
     * 提交考试并自动判分
     */
    public BigDecimal submitExam(Integer recordId, Map<Integer, String> answers) {
        if (recordId == null) {
            throw new BusinessException("考试记录ID不能为空");
        }

        // 查询考试记录
        ExamRecord record = examRecordDao.findById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }

        if (record.getStatus() == ExamStatus.SUBMITTED) {
            throw new BusinessException("考试已提交，不能重复提交");
        }

        // 获取试卷题目
        List<Question> questions = questionDao.findByPaperId(record.getPaperId());
        if (questions.isEmpty()) {
            throw new BusinessException("试卷没有题目");
        }

        // 自动判分
        BigDecimal totalScore = BigDecimal.ZERO;
        
        for (Question question : questions) {
            String studentAnswer = answers.get(question.getQuestionId());
            boolean isCorrect = checkAnswer(question, studentAnswer);
            
            BigDecimal score = BigDecimal.ZERO;
            if (isCorrect) {
                score = BigDecimal.valueOf(question.getScore());
                totalScore = totalScore.add(score);
            }

            // 保存答题记录
            AnswerRecord answerRecord = new AnswerRecord(recordId, question.getQuestionId(), studentAnswer);
            answerRecord.setIsCorrect(isCorrect);
            answerRecord.setScore(score);
            examRecordDao.insertAnswerRecord(answerRecord);
        }

        // 更新考试记录
        record.submitExam();
        record.setScore(totalScore);
        record.setEndTime(LocalDateTime.now());
        examRecordDao.update(record);

        return totalScore;
    }

    /**
     * 检查答案是否正确
     */
    private boolean checkAnswer(Question question, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return false;
        }

        String correctAnswer = question.getCorrectAnswer().toUpperCase().trim();
        String answer = studentAnswer.toUpperCase().trim();

        switch (question.getQuestionType()) {
            case SINGLE:
            case JUDGE:
                // 单选题和判断题：答案必须完全一致
                return correctAnswer.equals(answer);
                
            case MULTIPLE:
                // 多选题：排序后比较
                char[] correctChars = correctAnswer.toCharArray();
                char[] answerChars = answer.toCharArray();
                java.util.Arrays.sort(correctChars);
                java.util.Arrays.sort(answerChars);
                return java.util.Arrays.equals(correctChars, answerChars);
                
            default:
                return false;
        }
    }

    /**
     * 查询学生的考试记录
     */
    public List<ExamRecord> getStudentExamRecords(Integer studentId) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        List<ExamRecord> records = examRecordDao.findByStudentId(studentId);
        // 为每个记录加载试卷信息
        for (ExamRecord record : records) {
            Paper paper = paperDao.findById(record.getPaperId());
            record.setPaper(paper);
        }
        return records;
    }
    
    /**
     * 根据ID查询考试记录
     */
    public ExamRecord getExamRecordById(Integer recordId) {
        if (recordId == null) {
            throw new BusinessException("考试记录ID不能为空");
        }
        ExamRecord record = examRecordDao.findById(recordId);
        if (record != null) {
            // 加载试卷信息
            Paper paper = paperDao.findById(record.getPaperId());
            record.setPaper(paper);
        }
        return record;
    }
    
    /**
     * 查询答题记录
     */
    public List<AnswerRecord> getAnswerRecords(Integer recordId) {
        if (recordId == null) {
            throw new BusinessException("考试记录ID不能为空");
        }
        List<AnswerRecord> answerRecords = examRecordDao.findAnswerRecords(recordId);
        // 为每个答题记录加载题目信息
        for (AnswerRecord ar : answerRecords) {
            Question question = questionDao.findById(ar.getQuestionId());
            ar.setQuestion(question);
        }
        return answerRecords;
    }

    /**
     * 查询考试详情（包含答题记录）
     */
    public ExamRecord getExamDetail(Integer recordId) {
        if (recordId == null) {
            throw new BusinessException("考试记录ID不能为空");
        }

        ExamRecord record = examRecordDao.findById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }

        // 加载答题记录
        List<AnswerRecord> answerRecords = examRecordDao.findAnswerRecords(recordId);
        
        // 将答题记录转换为Map
        for (AnswerRecord answer : answerRecords) {
            record.recordAnswer(answer.getQuestionId(), answer.getStudentAnswer());
        }

        return record;
    }

    /**
     * 查询试卷的所有考试记录
     */
    public List<ExamRecord> getPaperExamRecords(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        return examRecordDao.findByPaperId(paperId);
    }

    /**
     * 超时自动交卷
     */
    public void timeoutSubmit(Integer recordId) {
        if (recordId == null) {
            throw new BusinessException("考试记录ID不能为空");
        }

        ExamRecord record = examRecordDao.findById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }

        if (record.getStatus() != ExamStatus.IN_PROGRESS) {
            return; // 已提交或未开始，不处理
        }

        // 设置为超时状态
        record.setStatus(ExamStatus.TIMEOUT);
        record.setEndTime(LocalDateTime.now());
        examRecordDao.update(record);
    }
}
