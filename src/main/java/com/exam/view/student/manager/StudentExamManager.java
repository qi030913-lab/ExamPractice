package com.exam.view.student.manager;

import com.exam.model.Paper;
import com.exam.model.User;
import com.exam.service.PaperService;
import com.exam.util.UIUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;

/**
 * 学生端考试管理器 - 处理所有与考试相关的操作
 * 性能优化版本：添加缓存和异步加载
 */
public class StudentExamManager {
    private final User student;
    private final PaperService paperService;
    
    // 缓存已发布的试卷列表，避免重复查询
    private static volatile List<Paper> cachedPapers = null;
    private static volatile long lastCacheTime = 0;
    private static final long CACHE_EXPIRE_MS = 60000; // 缓存过期时间：1分钟

    public StudentExamManager(User student) {
        this.student = student;
        this.paperService = new PaperService();
    }

    /**
     * 根据科目加载试卷（性能优化版本）
     * 使用缓存和异步加载，避免界面卡顿
     * @param subject 科目名称，"全部"表示所有科目
     * @param tableModel 表格模型
     * @param parentComponent 父组件，用于显示消息框
     */
    public void loadPapersBySubject(String subject, DefaultTableModel tableModel, JComponent parentComponent) {
        loadPapersBySubject(subject, tableModel, parentComponent, null);
    }
    
    /**
     * 根据科目加载试卷（带回调版本）
     * 使用缓存和异步加载，避免界面卡顿
     * @param subject 科目名称，"全部"表示所有科目
     * @param tableModel 表格模型
     * @param parentComponent 父组件，用于显示消息框
     * @param onComplete 加载完成后的回调，可为null
     */
    public void loadPapersBySubject(String subject, DefaultTableModel tableModel, JComponent parentComponent, Runnable onComplete) {
        if (tableModel == null) {
            return;
        }
        tableModel.setRowCount(0);
        
        // 使用SwingWorker异步加载，避免UI卡顿
        SwingWorker<List<Paper>, Void> worker = new SwingWorker<List<Paper>, Void>() {
            @Override
            protected List<Paper> doInBackground() {
                return loadPapersFromCacheOrDB();
            }
            
            @Override
            protected void done() {
                try {
                    List<Paper> allPapers = get();
                    populateTable(allPapers, subject, tableModel);
                } catch (Exception e) {
                    e.printStackTrace();
                    UIUtil.showError(parentComponent, "加载试卷失败：" + e.getMessage());
                } finally {
                    // 数据加载完成后执行回调，通知UI更新显示状态
                    if (onComplete != null) {
                        SwingUtilities.invokeLater(onComplete);
                    }
                }
            }
        };
        worker.execute();
    }
    
    /**
     * 从缓存或数据库加载试卷
     */
    private List<Paper> loadPapersFromCacheOrDB() {
        long now = System.currentTimeMillis();
        // 检查缓存是否有效
        if (cachedPapers != null && (now - lastCacheTime) < CACHE_EXPIRE_MS) {
            return cachedPapers;
        }
        
        // 缓存无效，从数据库加载（使用优化后的方法）
        List<Paper> papers = paperService.getAllPublishedPapersOptimized();
        cachedPapers = new CopyOnWriteArrayList<>(papers);
        lastCacheTime = now;
        return cachedPapers;
    }
    
    /**
     * 填充表格数据
     */
    private void populateTable(List<Paper> allPapers, String subject, DefaultTableModel tableModel) {
        List<Paper> filteredPapers;
        
        if ("全部".equals(subject)) {
            filteredPapers = allPapers;
        } else {
            filteredPapers = new java.util.ArrayList<>();
            for (Paper p : allPapers) {
                String paperSubject = p.getSubject() != null ? p.getSubject().trim() : "";
                String filterSubject = subject != null ? subject.trim() : "";
                if (isSubjectMatch(paperSubject, filterSubject)) {
                    filteredPapers.add(p);
                }
            }
        }

        for (Paper p : filteredPapers) {
            // 直接使用预计算的题型统计，避免重复Stream操作
            int singleCount = p.getSingleCount();
            int multipleCount = p.getMultipleCount();
            int judgeCount = p.getJudgeCount();
            int blankCount = p.getBlankCount();

            Object[] row = {
                    p.getPaperName(),
                    singleCount > 0 ? String.valueOf(singleCount) : "无",
                    multipleCount > 0 ? String.valueOf(multipleCount) : "无",
                    judgeCount > 0 ? String.valueOf(judgeCount) : "无",
                    blankCount > 0 ? String.valueOf(blankCount) : "无",
                    "开始考试"
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * 清除缓存（用于刷新操作）
     */
    public static void clearCache() {
        cachedPapers = null;
        lastCacheTime = 0;
    }

    /**
     * 开始考试
     * @param selectedRow 选中的行
     * @param tableModel 表格模型
     * @param table 表格组件，用于显示消息框
     */
    public void startExam(int selectedRow, DefaultTableModel tableModel, JTable table) {
        if (selectedRow == -1) {
            UIUtil.showWarning(table, "请先选择要学习的试卷");
            return;
        }

        String paperName = (String) tableModel.getValueAt(selectedRow, 0);
        if (paperName == null || paperName.isEmpty()) {
            UIUtil.showWarning(table, "请选择有效的试卷");
            return;
        }

        if (!UIUtil.showConfirm(table, "确定要开始考试《" + paperName + "》吗？\n考试开始后将开始计时。")) {
            return;
        }

        try {
            Paper paper = paperService.getPaperByName(paperName);
            if (paper == null || paper.getQuestions().isEmpty()) {
                UIUtil.showError(table, "该试卷没有题目，无法考试");
                return;
            }

            // 打开考试界面
            com.exam.service.ExamService examService = new com.exam.service.ExamService();
            new com.exam.view.student.ExamFrame(student, paper, examService).setVisible(true);

        } catch (Exception e) {
            UIUtil.showError(table, "开始考试失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isSubjectMatch(String paperSubject, String filterSubject) {
        // 基本的非空检查
        if (paperSubject == null || filterSubject == null) {
            return paperSubject == filterSubject;
        }
        
        // 标准化比较 - 去除空格并转换为小写
        String normalizedPaperSubject = paperSubject.trim().toLowerCase();
        String normalizedFilterSubject = filterSubject.trim().toLowerCase();
        
        // 精确匹配
        if (normalizedPaperSubject.equals(normalizedFilterSubject)) {
            return true;
        }
        
        // 处理一些常见的中文字符差异（如果需要）
        // 例如：可能存在的全角/半角字符差异
        normalizedPaperSubject = normalizedPaperSubject.replaceAll("\\s+", "");
        normalizedFilterSubject = normalizedFilterSubject.replaceAll("\\s+", "");
        
        return normalizedPaperSubject.equals(normalizedFilterSubject);
    }
}