package com.exam.service;

import com.exam.dao.ExamRecordDao;
import com.exam.dao.PaperDao;
import com.exam.dao.QuestionDao;
import com.exam.exception.BusinessException;
import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.model.enums.ExamStatus;
import com.exam.util.DBUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ExamService {
    private final ExamRecordDao examRecordDao;
    private final PaperDao paperDao;
    private final QuestionDao questionDao;
    private final Object[] startExamLocks = createStartExamLocks();

    public ExamService() {
        this.examRecordDao = new ExamRecordDao();
        this.paperDao = new PaperDao();
        this.questionDao = new QuestionDao();
    }

    public ExamRecord startExam(Integer studentId, Integer paperId) {
        return startOrResumeExam(studentId, paperId).getRecord();
    }

    public ExamStartResult startOrResumeExam(Integer studentId, Integer paperId) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }

        Paper paper = paperDao.findById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }

        synchronized (resolveStartExamLock(studentId, paperId)) {
            ExamRecord existingRecord = findExistingInProgressRecord(studentId, paperId);
            if (existingRecord != null) {
                existingRecord.setPaper(paper);
                return new ExamStartResult(existingRecord, true);
            }

            ExamRecord record = new ExamRecord(studentId, paperId);
            record.startExam();
            record.setPaper(paper);

            int recordId = examRecordDao.insert(record);
            record.setRecordId(recordId);

            return new ExamStartResult(record, false);
        }
    }

    public BigDecimal submitExam(Integer recordId, Map<Integer, String> answers) {
        if (recordId == null) {
            throw new BusinessException("考试记录ID不能为空");
        }

        Map<Integer, String> answerMap = answers == null ? Collections.emptyMap() : answers;

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ExamRecord record = examRecordDao.findByIdForUpdate(conn, recordId);
                if (record == null) {
                    throw new BusinessException("考试记录不存在");
                }
                if (record.getStatus() == ExamStatus.SUBMITTED) {
                    throw new BusinessException("考试已提交，不能重复提交");
                }

                List<Question> questions = questionDao.findByPaperId(record.getPaperId());
                if (questions.isEmpty()) {
                    throw new BusinessException("试卷没有题目");
                }

                BigDecimal totalScore = BigDecimal.ZERO;
                List<AnswerRecord> answerRecords = new ArrayList<>();

                for (Question question : questions) {
                    String studentAnswer = answerMap.get(question.getQuestionId());
                    boolean isCorrect = checkAnswer(question, studentAnswer);

                    BigDecimal score = BigDecimal.ZERO;
                    if (isCorrect) {
                        score = BigDecimal.valueOf(question.getScore());
                        totalScore = totalScore.add(score);
                    }

                    AnswerRecord answerRecord = new AnswerRecord(recordId, question.getQuestionId(), studentAnswer);
                    answerRecord.setIsCorrect(isCorrect);
                    answerRecord.setScore(score);
                    answerRecords.add(answerRecord);
                }

                examRecordDao.insertAnswerRecordsBatch(conn, answerRecords);

                record.submitExam();
                record.setScore(totalScore);
                record.setEndTime(LocalDateTime.now());
                examRecordDao.update(conn, record);

                conn.commit();
                return totalScore;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new BusinessException("提交考试失败: " + e.getMessage());
        }
    }

    private boolean checkAnswer(Question question, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return false;
        }

        String correctAnswer = question.getCorrectAnswer().toUpperCase().trim();
        String answer = studentAnswer.toUpperCase().trim();

        switch (question.getQuestionType()) {
            case SINGLE:
            case JUDGE:
                return correctAnswer.equals(answer);
            case MULTIPLE:
                char[] correctChars = correctAnswer.toCharArray();
                char[] answerChars = answer.toCharArray();
                java.util.Arrays.sort(correctChars);
                java.util.Arrays.sort(answerChars);
                return java.util.Arrays.equals(correctChars, answerChars);
            default:
                return false;
        }
    }

    public List<ExamRecord> getStudentExamRecords(Integer studentId) {
        return getStudentExamRecordsOptimized(studentId);
    }

    public List<ExamRecord> getStudentExamRecordsOptimized(Integer studentId) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        return examRecordDao.findByStudentIdWithPaper(studentId);
    }

    public Map<Integer, List<ExamRecord>> getStudentExamRecordsByStudentIds(Collection<Integer> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        for (Integer studentId : studentIds) {
            if (studentId == null) {
                throw new BusinessException("学生ID不能为空");
            }
        }
        return examRecordDao.findByStudentIdsWithPaper(studentIds);
    }

    public List<ExamRecord> getStudentExamRecordsPaginated(Integer studentId, int pageNum, int pageSize) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        if (pageNum <= 0) {
            throw new BusinessException("页码必须从1开始");
        }
        if (pageSize <= 0 || pageSize > 200) {
            throw new BusinessException("每页大小必须在1-200之间");
        }
        return examRecordDao.findByStudentIdWithPaperPaginated(studentId, pageNum, pageSize);
    }

    public int getStudentExamRecordCount(Integer studentId) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        return examRecordDao.countByStudentId(studentId);
    }

    public ExamRecord getExamRecordById(Integer recordId) {
        if (recordId == null) {
            throw new BusinessException("考试记录ID不能为空");
        }
        return examRecordDao.findByIdWithPaper(recordId);
    }

    public List<AnswerRecord> getAnswerRecords(Integer recordId) {
        if (recordId == null) {
            throw new BusinessException("考试记录ID不能为空");
        }
        List<AnswerRecord> answerRecords = examRecordDao.findAnswerRecords(recordId);
        if (answerRecords.isEmpty()) {
            return answerRecords;
        }

        Set<Integer> questionIds = new HashSet<>();
        for (AnswerRecord answerRecord : answerRecords) {
            questionIds.add(answerRecord.getQuestionId());
        }

        Map<Integer, Question> questionMap = questionDao.findByIds(questionIds);
        for (AnswerRecord answerRecord : answerRecords) {
            answerRecord.setQuestion(questionMap.get(answerRecord.getQuestionId()));
        }
        return answerRecords;
    }

    public Map<Integer, List<AnswerRecord>> getAnswerRecordsBatch(List<Integer> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            return new java.util.HashMap<>();
        }

        Map<Integer, List<AnswerRecord>> answerRecordsMap = examRecordDao.findAnswerRecordsByRecordIds(recordIds);

        Set<Integer> questionIds = new HashSet<>();
        for (List<AnswerRecord> records : answerRecordsMap.values()) {
            for (AnswerRecord answerRecord : records) {
                questionIds.add(answerRecord.getQuestionId());
            }
        }

        Map<Integer, Question> questionMap = questionDao.findByIds(questionIds);
        for (List<AnswerRecord> records : answerRecordsMap.values()) {
            for (AnswerRecord answerRecord : records) {
                answerRecord.setQuestion(questionMap.get(answerRecord.getQuestionId()));
            }
        }

        return answerRecordsMap;
    }

    public ExamRecord getExamDetail(Integer recordId) {
        if (recordId == null) {
            throw new BusinessException("考试记录ID不能为空");
        }

        ExamRecord record = examRecordDao.findByIdWithPaper(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }

        List<AnswerRecord> answerRecords = examRecordDao.findAnswerRecords(recordId);
        for (AnswerRecord answerRecord : answerRecords) {
            record.recordAnswer(answerRecord.getQuestionId(), answerRecord.getStudentAnswer());
        }

        return record;
    }

    public List<ExamRecord> getPaperExamRecords(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        return examRecordDao.findByPaperId(paperId);
    }

    public void timeoutSubmit(Integer recordId) {
        if (recordId == null) {
            throw new BusinessException("考试记录ID不能为空");
        }

        ExamRecord record = examRecordDao.findById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }

        if (record.getStatus() != ExamStatus.IN_PROGRESS) {
            return;
        }

        record.setStatus(ExamStatus.TIMEOUT);
        record.setEndTime(LocalDateTime.now());
        examRecordDao.update(record);
    }

    private ExamRecord findExistingInProgressRecord(Integer studentId, Integer paperId) {
        List<ExamRecord> records = examRecordDao.findByStudentId(studentId);
        for (ExamRecord record : records) {
            if (paperId.equals(record.getPaperId()) && record.getStatus() == ExamStatus.IN_PROGRESS) {
                return record;
            }
        }
        return null;
    }

    private Object[] createStartExamLocks() {
        Object[] locks = new Object[64];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new Object();
        }
        return locks;
    }

    private Object resolveStartExamLock(Integer studentId, Integer paperId) {
        int hash = 31 * studentId.hashCode() + paperId.hashCode();
        return startExamLocks[Math.floorMod(hash, startExamLocks.length)];
    }

    public static class ExamStartResult {
        private final ExamRecord record;
        private final boolean resumed;

        public ExamStartResult(ExamRecord record, boolean resumed) {
            this.record = record;
            this.resumed = resumed;
        }

        public ExamRecord getRecord() {
            return record;
        }

        public boolean isResumed() {
            return resumed;
        }
    }
}
