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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    public ExamService(ExamRecordDao examRecordDao, PaperDao paperDao, QuestionDao questionDao) {
        this.examRecordDao = examRecordDao;
        this.paperDao = paperDao;
        this.questionDao = questionDao;
    }

    public ExamRecord startExam(Integer studentId, Integer paperId) {
        return startOrResumeExam(studentId, paperId).getRecord();
    }

    public ExamStartResult startOrResumeExam(Integer studentId, Integer paperId) {
        if (studentId == null) {
            throw new BusinessException("Student ID cannot be null");
        }
        if (paperId == null) {
            throw new BusinessException("Paper ID cannot be null");
        }

        Paper paper = paperDao.findById(paperId);
        if (paper == null) {
            throw new BusinessException("Paper does not exist");
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

    @Transactional
    public BigDecimal submitExam(Integer recordId, Map<Integer, String> answers) {
        if (recordId == null) {
            throw new BusinessException("Exam record ID cannot be null");
        }

        Map<Integer, String> answerMap = answers == null ? Collections.emptyMap() : answers;

        ExamRecord record = examRecordDao.findByIdForUpdate(recordId);
        requireRecordForSubmit(record);

        List<Question> questions = requireQuestions(record.getPaperId());
        SettlementResult settlement = buildSettlementResult(recordId, questions, answerMap);
        examRecordDao.insertAnswerRecordsBatch(settlement.getAnswerRecords());

        record.submitExam();
        record.setScore(settlement.getTotalScore());
        record.setEndTime(LocalDateTime.now());
        examRecordDao.update(record);

        return settlement.getTotalScore();
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
            throw new BusinessException("Student ID cannot be null");
        }
        return examRecordDao.findByStudentIdWithPaper(studentId);
    }

    public Map<Integer, List<ExamRecord>> getStudentExamRecordsByStudentIds(Collection<Integer> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        for (Integer studentId : studentIds) {
            if (studentId == null) {
                throw new BusinessException("Student ID cannot be null");
            }
        }
        return examRecordDao.findByStudentIdsWithPaper(studentIds);
    }

    public List<ExamRecord> getStudentExamRecordsPaginated(Integer studentId, int pageNum, int pageSize) {
        if (studentId == null) {
            throw new BusinessException("Student ID cannot be null");
        }
        if (pageNum <= 0) {
            throw new BusinessException("Page number must start from 1");
        }
        if (pageSize <= 0 || pageSize > 200) {
            throw new BusinessException("Page size must be between 1 and 200");
        }
        int offset = (pageNum - 1) * pageSize;
        return examRecordDao.findByStudentIdWithPaperPaginated(studentId, offset, pageSize);
    }

    public int getStudentExamRecordCount(Integer studentId) {
        if (studentId == null) {
            throw new BusinessException("Student ID cannot be null");
        }
        return examRecordDao.countByStudentId(studentId);
    }

    public ExamRecord getExamRecordById(Integer recordId) {
        if (recordId == null) {
            throw new BusinessException("Exam record ID cannot be null");
        }
        return examRecordDao.findByIdWithPaper(recordId);
    }

    public List<AnswerRecord> getAnswerRecords(Integer recordId) {
        if (recordId == null) {
            throw new BusinessException("Exam record ID cannot be null");
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
            throw new BusinessException("Exam record ID cannot be null");
        }

        ExamRecord record = examRecordDao.findByIdWithPaper(recordId);
        if (record == null) {
            throw new BusinessException("Exam record does not exist");
        }

        List<AnswerRecord> answerRecords = examRecordDao.findAnswerRecords(recordId);
        for (AnswerRecord answerRecord : answerRecords) {
            record.recordAnswer(answerRecord.getQuestionId(), answerRecord.getStudentAnswer());
        }

        return record;
    }

    public List<ExamRecord> getPaperExamRecords(Integer paperId) {
        if (paperId == null) {
            throw new BusinessException("Paper ID cannot be null");
        }
        return examRecordDao.findByPaperId(paperId);
    }

    @Transactional
    public void timeoutSubmit(Integer recordId) {
        if (recordId == null) {
            throw new BusinessException("Exam record ID cannot be null");
        }

        ExamRecord record = examRecordDao.findByIdForUpdate(recordId);
        if (record == null) {
            throw new BusinessException("Exam record does not exist");
        }
        if (record.getStatus() != ExamStatus.IN_PROGRESS) {
            return;
        }

        List<Question> questions = requireQuestions(record.getPaperId());
        SettlementResult settlement = buildSettlementResult(recordId, questions, Collections.emptyMap());
        examRecordDao.insertAnswerRecordsBatch(settlement.getAnswerRecords());

        record.setStatus(ExamStatus.TIMEOUT);
        record.setScore(settlement.getTotalScore());
        record.setEndTime(LocalDateTime.now());
        examRecordDao.update(record);
    }

    private ExamRecord findExistingInProgressRecord(Integer studentId, Integer paperId) {
        return examRecordDao.findInProgressByStudentIdAndPaperId(studentId, paperId, ExamStatus.IN_PROGRESS);
    }

    private void requireRecordForSubmit(ExamRecord record) {
        if (record == null) {
            throw new BusinessException("Exam record does not exist");
        }
        if (record.getStatus() == ExamStatus.IN_PROGRESS) {
            return;
        }
        if (record.getStatus() == ExamStatus.SUBMITTED) {
            throw new BusinessException("Exam already submitted");
        }
        if (record.getStatus() == ExamStatus.TIMEOUT) {
            throw new BusinessException("Exam already timed out");
        }
        throw new BusinessException("Exam is not in progress");
    }

    private List<Question> requireQuestions(Integer paperId) {
        List<Question> questions = questionDao.findByPaperId(paperId);
        if (questions.isEmpty()) {
            throw new BusinessException("Paper has no questions");
        }
        return questions;
    }

    private SettlementResult buildSettlementResult(Integer recordId, List<Question> questions, Map<Integer, String> answerMap) {
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

        return new SettlementResult(totalScore, answerRecords);
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

    private static class SettlementResult {
        private final BigDecimal totalScore;
        private final List<AnswerRecord> answerRecords;

        private SettlementResult(BigDecimal totalScore, List<AnswerRecord> answerRecords) {
            this.totalScore = totalScore;
            this.answerRecords = answerRecords;
        }

        private BigDecimal getTotalScore() {
            return totalScore;
        }

        private List<AnswerRecord> getAnswerRecords() {
            return answerRecords;
        }
    }
}
