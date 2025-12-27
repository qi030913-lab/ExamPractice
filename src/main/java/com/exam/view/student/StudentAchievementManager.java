package com.exam.view.student;

import com.exam.model.User;
import com.exam.model.ExamRecord;
import com.exam.model.AnswerRecord;
import com.exam.service.ExamService;
import com.exam.util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 学生端成就管理器 - 处理所有与成就统计相关的操作
 */
public class StudentAchievementManager {
    private final User student;
    private final ExamService examService;

    public StudentAchievementManager(User student) {
        this.student = student;
        this.examService = new ExamService();
    }

    /**
     * 获取统计数据
     * @return 包含统计数据的数组 [考试次数, 平均分, 正确题数, 正确率]
     */
    public double[] getStatistics() {
        try {
            List<ExamRecord> records = examService.getStudentExamRecords(student.getUserId());
            
            // 统计数据
            int totalExams = records.size();
            double avgScore = records.stream()
                .filter(r -> r.getScore() != null)
                .mapToDouble(r -> r.getScore().doubleValue())
                .average()
                .orElse(0.0);
            long totalCorrect = 0;
            long totalQuestions = 0;
            
            for (ExamRecord record : records) {
                List<AnswerRecord> answerRecords = examService.getAnswerRecords(record.getRecordId());
                totalCorrect += answerRecords.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect()).count();
                totalQuestions += answerRecords.size();
            }
            
            double accuracy = totalQuestions > 0 ? (totalCorrect * 100.0 / totalQuestions) : 0;

            return new double[] {totalExams, avgScore, totalCorrect, accuracy};
        } catch (Exception e) {
            e.printStackTrace();
            return new double[] {0, 0, 0, 0};
        }
    }

    /**
     * 获取考试记录用于绘制图表
     * @return 考试记录列表
     */
    public List<ExamRecord> getExamRecords() {
        try {
            return examService.getStudentExamRecords(student.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 获取题型准确率数据
     * @return 题型准确率数据数组，每行包含[单选正确数, 单选总数, 多选正确数, 多选总数, 判断正确数, 判断总数, 填空正确数, 填空总数]
     */
    public int[] getQuestionTypeAccuracy() {
        try {
            List<ExamRecord> records = examService.getStudentExamRecords(student.getUserId());
            int[] data = new int[8]; // [单选正确, 单选总数, 多选正确, 多选总数, 判断正确, 判断总数, 填空正确, 填空总数]

            for (ExamRecord record : records) {
                List<AnswerRecord> answerRecords = examService.getAnswerRecords(record.getRecordId());
                for (AnswerRecord ar : answerRecords) {
                    if (ar.getQuestion() != null) {
                        int index = -1;
                        switch (ar.getQuestion().getQuestionType()) {
                            case SINGLE:
                                index = 0; // 单选题
                                break;
                            case MULTIPLE:
                                index = 2; // 多选题
                                break;
                            case JUDGE:
                                index = 4; // 判断题
                                break;
                            case BLANK:
                                index = 6; // 填空题
                                break;
                        }
                        if (index >= 0) {
                            data[index + 1]++; // 总数增加
                            if (ar.getIsCorrect() != null && ar.getIsCorrect()) {
                                data[index]++; // 正确数增加
                            }
                        }
                    }
                }
            }

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return new int[8];
        }
    }
}