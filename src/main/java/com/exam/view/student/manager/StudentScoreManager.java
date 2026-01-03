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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 学生端成绩管理器 - 处理所有与成绩相关的操作
 * 性能优化版本：添加缓存和异步加载
 */
public class StudentScoreManager {
    private final User student;
    private final ExamService examService;
    
    // 缓存考试详情，避免重复查询
    private static final Map<Integer, ExamRecord> examDetailCache = new ConcurrentHashMap<>();
    private static final Map<Integer, List<AnswerRecord>> answerRecordCache = new ConcurrentHashMap<>();

    public StudentScoreManager(User student) {
        this.student = student;
        this.examService = new ExamService();
    }

    /**
     * 加载成绩数据（性能优化版本 - 异步加载）
     * @param tableModel 表格模型
     * @param parentComponent 父组件，用于显示消息框
     * @param cachedRecords 缓存列表，用于存储查询结果
     * @param onComplete 加载完成回调
     */
    public void loadScores(DefaultTableModel tableModel, JComponent parentComponent, 
                          List<ExamRecord> cachedRecords, Runnable onComplete) {
        tableModel.setRowCount(0);
        
        // 使用SwingWorker异步加载，避免UI卡顿
        SwingWorker<List<ExamRecord>, Void> worker = new SwingWorker<List<ExamRecord>, Void>() {
            @Override
            protected List<ExamRecord> doInBackground() {
                // 使用优化后的方法，一次性查询考试记录和试卷信息
                List<ExamRecord> result = examService.getStudentExamRecordsOptimized(student.getUserId());
                
                if (!result.isEmpty()) {
                    // 批量查询所有考试记录的答题记录，避免循环查询
                    List<Integer> recordIds = new ArrayList<>();
                    for (ExamRecord r : result) {
                        recordIds.add(r.getRecordId());
                    }
                    java.util.Map<Integer, List<AnswerRecord>> answerRecordsMap = 
                        examService.getAnswerRecordsBatch(recordIds);
                    
                    // 缓存答题记录
                    answerRecordCache.putAll(answerRecordsMap);
                }
                
                return result;
            }
            
            @Override
            protected void done() {
                try {
                    List<ExamRecord> result = get();
                    // 更新缓存列表
                    cachedRecords.clear();
                    cachedRecords.addAll(result);
                    // 填充表格
                    populateScoreTable(result, tableModel);
                    // 调用完成回调
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } catch (Exception e) {
                    UIUtil.showError(parentComponent, "加载成绩失败：" + e.getMessage());
                    e.printStackTrace();
                    // 即使失败也要调用回调
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        };
        worker.execute();
    }
    
    /**
     * 填充成绩表格数据
     */
    private void populateScoreTable(List<ExamRecord> records, DefaultTableModel tableModel) {
        if (records.isEmpty()) {
            return;
        }
        
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
            
            // 从缓存中获取答题记录
            List<AnswerRecord> answerRecords = answerRecordCache.getOrDefault(record.getRecordId(), new ArrayList<>());
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
    }

    /**
     * 显示考试详情对话框（性能优化版本 - 使用缓存）
     * @param recordId 考试记录ID
     * @param parentComponent 父组件，用于显示对话框
     */
    public void showExamDetail(int recordId, JComponent parentComponent) {
        // 先显示加载提示
        JDialog loadingDialog = createLoadingDialog(parentComponent, "加载中...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private ExamRecord record;
            private List<AnswerRecord> answerRecords;
            
            @Override
            protected Void doInBackground() {
                // 优先从缓存获取
                record = examDetailCache.get(recordId);
                if (record == null) {
                    record = examService.getExamRecordById(recordId);
                    if (record != null) {
                        examDetailCache.put(recordId, record);
                    }
                }
                
                answerRecords = answerRecordCache.get(recordId);
                if (answerRecords == null) {
                    answerRecords = examService.getAnswerRecords(recordId);
                    answerRecordCache.put(recordId, answerRecords);
                }
                
                return null;
            }
            
            @Override
            protected void done() {
                loadingDialog.dispose();
                
                if (record == null) {
                    UIUtil.showError(parentComponent, "找不到考试记录");
                    return;
                }
                
                showDetailDialog(record, answerRecords, parentComponent);
            }
        };
        worker.execute();
        loadingDialog.setVisible(true);
    }
    
    /**
     * 创建加载提示对话框
     */
    private JDialog createLoadingDialog(JComponent parent, String message) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "", true);
        dialog.setUndecorated(true);
        dialog.setSize(150, 50);
        dialog.setLocationRelativeTo(parent);
        
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        dialog.add(label);
        
        return dialog;
    }
    
    /**
     * 显示详情对话框
     */
    private void showDetailDialog(ExamRecord record, List<AnswerRecord> answerRecords, JComponent parentComponent) {
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
    }
    
    /**
     * 清除缓存
     */
    public static void clearCache() {
        examDetailCache.clear();
        answerRecordCache.clear();
    }
}