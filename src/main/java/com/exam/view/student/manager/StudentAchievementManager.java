package com.exam.view.student.manager;

import com.exam.model.User;
import com.exam.model.ExamRecord;
import com.exam.model.AnswerRecord;
import com.exam.service.ExamService;
import com.exam.util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 学生端成就管理器 - 处理所有与成就统计相关的操作
 * 性能优化版本：使用静态缓存，避免重复查询
 */
public class StudentAchievementManager {
    private final User student;
    private final ExamService examService;
    
    // 静态缓存数据，避免重复查询（跨面板实例保持）
    private static volatile List<ExamRecord> cachedRecords;
    private static volatile java.util.Map<Integer, List<AnswerRecord>> cachedAnswerRecordsMap;
    private static volatile Integer cachedUserId = null;
    private static volatile boolean dataCached = false;
    private static volatile long lastCacheTime = 0;
    private static final long CACHE_EXPIRE_MS = 60000; // 缓存过期时间：1分钟
    
    // 预计算的统计数据，避免paintComponent重复计算
    private static volatile double[] cachedStats;
    private static volatile int[] cachedQuestionTypeAccuracy;

    public StudentAchievementManager(User student) {
        this.student = student;
        this.examService = new ExamService();
    }
    
    /**
     * 加载并缓存数据（性能优化版本）
     */
    private void loadDataIfNeeded() {
        long now = System.currentTimeMillis();
        
        // 检查缓存是否有效（同一用户且未过期）
        if (dataCached && cachedUserId != null && cachedUserId.equals(student.getUserId()) 
                && (now - lastCacheTime) < CACHE_EXPIRE_MS) {
            return; // 缓存有效，无需重新加载
        }
        
        try {
            // 使用优化后的方法
            cachedRecords = new CopyOnWriteArrayList<>(examService.getStudentExamRecordsOptimized(student.getUserId()));
            
            if (!cachedRecords.isEmpty()) {
                // 批量查询所有答题记录
                List<Integer> recordIds = new java.util.ArrayList<>();
                for (ExamRecord r : cachedRecords) {
                    recordIds.add(r.getRecordId());
                }
                cachedAnswerRecordsMap = examService.getAnswerRecordsBatch(recordIds);
            } else {
                cachedAnswerRecordsMap = new java.util.concurrent.ConcurrentHashMap<>();
            }
            
            // 预计算统计数据，避免每次绘图时重复计算
            calculateStatistics();
            calculateQuestionTypeAccuracy();
            
            cachedUserId = student.getUserId();
            dataCached = true;
            lastCacheTime = now;
        } catch (Exception e) {
            e.printStackTrace();
            cachedRecords = new CopyOnWriteArrayList<>();
            cachedAnswerRecordsMap = new java.util.concurrent.ConcurrentHashMap<>();
            cachedStats = new double[] {0, 0, 0, 0};
            cachedQuestionTypeAccuracy = new int[8];
        }
    }
    
    /**
     * 预计算统计数据
     */
    private void calculateStatistics() {
        int totalExams = cachedRecords.size();
        double avgScore = cachedRecords.stream()
            .filter(r -> r.getScore() != null)
            .mapToDouble(r -> r.getScore().doubleValue())
            .average()
            .orElse(0.0);
        
        if (cachedRecords.isEmpty()) {
            cachedStats = new double[] {0, 0, 0, 0};
            return;
        }
        
        long totalCorrect = 0;
        long totalQuestions = 0;
        
        for (ExamRecord record : cachedRecords) {
            List<AnswerRecord> answerRecords = cachedAnswerRecordsMap.getOrDefault(record.getRecordId(), new java.util.ArrayList<>());
            totalCorrect += answerRecords.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect()).count();
            totalQuestions += answerRecords.size();
        }
        
        double accuracy = totalQuestions > 0 ? (totalCorrect * 100.0 / totalQuestions) : 0;
        cachedStats = new double[] {totalExams, avgScore, totalCorrect, accuracy};
    }
    
    /**
     * 预计算题型准确率
     */
    private void calculateQuestionTypeAccuracy() {
        int[] data = new int[8];
        
        if (cachedRecords.isEmpty()) {
            cachedQuestionTypeAccuracy = data;
            return;
        }

        for (ExamRecord record : cachedRecords) {
            List<AnswerRecord> answerRecords = cachedAnswerRecordsMap.getOrDefault(record.getRecordId(), new java.util.ArrayList<>());
            for (AnswerRecord ar : answerRecords) {
                if (ar.getQuestion() != null && ar.getQuestion().getQuestionType() != null) {
                    int index = -1;
                    switch (ar.getQuestion().getQuestionType()) {
                        case SINGLE:
                            index = 0;
                            break;
                        case MULTIPLE:
                            index = 2;
                            break;
                        case JUDGE:
                            index = 4;
                            break;
                        case BLANK:
                            index = 6;
                            break;
                    }
                    if (index >= 0) {
                        data[index + 1]++;
                        if (ar.getIsCorrect() != null && ar.getIsCorrect()) {
                            data[index]++;
                        }
                    }
                }
            }
        }
        
        cachedQuestionTypeAccuracy = data;
    }

    /**
     * 获取统计数据（性能优化版本 - 直接返回预计算结果）
     * @return 包含统计数据的数组 [考试次数, 平均分, 正确题数, 正确率]
     */
    public double[] getStatistics() {
        loadDataIfNeeded();
        return cachedStats != null ? cachedStats : new double[] {0, 0, 0, 0};
    }

    /**
     * 获取考试记录用于绘制图表（性能优化版本 - 直接返回缓存）
     * @return 考试记录列表
     */
    public List<ExamRecord> getExamRecords() {
        loadDataIfNeeded();
        return cachedRecords != null ? cachedRecords : new java.util.ArrayList<>();
    }

    /**
     * 获取题型准确率数据（性能优化版本 - 直接返回预计算结果）
     * @return 题型准确率数据数组
     */
    public int[] getQuestionTypeAccuracy() {
        loadDataIfNeeded();
        return cachedQuestionTypeAccuracy != null ? cachedQuestionTypeAccuracy : new int[8];
    }
    
    /**
     * 清除缓存（用于刷新操作）
     */
    public static void clearCache() {
        cachedRecords = null;
        cachedAnswerRecordsMap = null;
        cachedStats = null;
        cachedQuestionTypeAccuracy = null;
        cachedUserId = null;
        dataCached = false;
        lastCacheTime = 0;
    }
}