package com.exam.view.student.manager;

import com.exam.model.User;
import com.exam.model.ExamRecord;
import com.exam.model.AnswerRecord;
import com.exam.service.ExamService;
import com.exam.util.UIUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.List;

/**
 * 学生端成绩管理器 - 处理所有与成绩相关的操作
 */
public class StudentScoreManager {
    private final User student;
    private final ExamService examService;

    public StudentScoreManager(User student) {
        this.student = student;
        this.examService = new ExamService();
    }

    /**
     * 加载成绩数据
     * @param tableModel 表格模型
     * @param parentComponent 父组件，用于显示消息框
     */
    public void loadScores(DefaultTableModel tableModel, JComponent parentComponent) {
        tableModel.setRowCount(0);
        try {
            List<ExamRecord> records = examService.getStudentExamRecords(student.getUserId());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            for (ExamRecord record : records) {
                // 计算考试时长
                String duration = "";
                if (record.getStartTime() != null && record.getSubmitTime() != null) {
                    Duration d = Duration.between(record.getStartTime(), record.getSubmitTime());
                    long minutes = d.toMinutes();
                    long seconds = d.getSeconds() % 60;
                    duration = String.format("%d分%d秒", minutes, seconds);
                }
                
                // 获取详细答题记录
                List<AnswerRecord> answerRecords = examService.getAnswerRecords(record.getRecordId());
                long correctCount = answerRecords.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect()).count();
                long wrongCount = answerRecords.stream().filter(a -> a.getIsCorrect() != null && !a.getIsCorrect()).count();
                
                Object[] row = {
                    record.getPaper() != null ? record.getPaper().getPaperName() : "未知",
                    record.getPaper() != null ? record.getPaper().getTotalScore() : 0,
                    record.getScore() != null ? record.getScore() : 0,
                    correctCount,
                    wrongCount,
                    record.getSubmitTime() != null ? record.getSubmitTime().format(formatter) : "",
                    duration,
                    "查看详情"
                };
                tableModel.addRow(row);
            }
            
            if (records.isEmpty()) {
                Object[] row = {"暂无考试记录", "", "", "", "", "", "", ""};
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            UIUtil.showError(parentComponent, "加载成绩失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 显示考试详情对话框
     * @param recordId 考试记录ID
     * @param parentComponent 父组件，用于显示对话框
     */
    public void showExamDetail(int recordId, JComponent parentComponent) {
        try {
            ExamRecord record = examService.getExamRecordById(recordId);
            if (record == null) {
                UIUtil.showError(parentComponent, "找不到考试记录");
                return;
            }
            
            List<AnswerRecord> answerRecords = examService.getAnswerRecords(recordId);
            
            // 创建对话框
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parentComponent), "考试详情", true);
            dialog.setSize(900, 600);
            dialog.setLocationRelativeTo(parentComponent);
            
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // 顶部信息
            JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 10));
            infoPanel.add(new JLabel("试卷名称：" + (record.getPaper() != null ? record.getPaper().getPaperName() : "未知")));
            infoPanel.add(new JLabel("总分：" + (record.getPaper() != null ? record.getPaper().getTotalScore() : 0) + " 分"));
            infoPanel.add(new JLabel("得分：" + (record.getScore() != null ? record.getScore() : 0) + " 分"));
            infoPanel.add(new JLabel("考试时间：" + (record.getSubmitTime() != null ? record.getSubmitTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "")));
            
            long correctCount = answerRecords.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect()).count();
            long wrongCount = answerRecords.stream().filter(a -> a.getIsCorrect() != null && !a.getIsCorrect()).count();
            infoPanel.add(new JLabel("正确题数：" + correctCount));
            infoPanel.add(new JLabel("错误题数：" + wrongCount));
            
            panel.add(infoPanel, BorderLayout.NORTH);
            
            // 错题详情表格
            String[] columns = {"题号", "题目类型", "题目内容", "正确答案", "你的答案", "是否正确"};
            DefaultTableModel detailModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            JTable detailTable = new JTable(detailModel);
            detailTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            detailTable.setRowHeight(35);
            detailTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
            
            // 填充答题数据
            int questionNo = 1;
            for (AnswerRecord ar : answerRecords) {
                if (ar.getQuestion() != null) {
                    String isCorrect = ar.getIsCorrect() != null ? (ar.getIsCorrect() ? "✓ 正确" : "✗ 错误") : "未答";
                    String content = ar.getQuestion().getContent();
                    if (content.length() > 30) {
                        content = content.substring(0, 30) + "...";
                    }
                    
                    Object[] row = {
                        questionNo++,
                        ar.getQuestion().getQuestionType() != null ? ar.getQuestion().getQuestionType().getDescription() : "",
                        content,
                        ar.getQuestion().getCorrectAnswer(),
                        ar.getStudentAnswer() != null ? ar.getStudentAnswer() : "未答",
                        isCorrect
                    };
                    detailModel.addRow(row);
                }
            }
            
            JScrollPane scrollPane = new JScrollPane(detailTable);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // 关闭按钮
            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(e -> dialog.dispose());
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.add(panel);
            dialog.setVisible(true);
            
        } catch (Exception e) {
            UIUtil.showError(parentComponent, "加载详情失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}