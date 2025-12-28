package com.exam.view.student.manager;

import com.exam.model.Paper;
import com.exam.model.User;
import com.exam.service.PaperService;
import com.exam.util.UIUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * 学生端考试管理器 - 处理所有与考试相关的操作
 */
public class StudentExamManager {
    private final User student;
    private final PaperService paperService;

    public StudentExamManager(User student) {
        this.student = student;
        this.paperService = new PaperService();
    }

    /**
     * 根据科目加载试卷
     * @param subject 科目名称，"全部"表示所有科目
     * @param tableModel 表格模型
     * @param parentComponent 父组件，用于显示消息框
     */
    public void loadPapersBySubject(String subject, DefaultTableModel tableModel, JComponent parentComponent) {
        System.out.println("DEBUG [StudentExamManager]: loadPapersBySubject() 被调用, 科目=" + subject);
        
        if (tableModel == null) {
            System.out.println("DEBUG [StudentExamManager]: tableModel为null，直接返回");
            return;
        }
        tableModel.setRowCount(0);
        try {
            List<Paper> allPapers = paperService.getAllPublishedPapers();
            System.out.println("DEBUG [StudentExamManager]: 从数据库获取到 " + allPapers.size() + " 个已发布试卷");
            
            // 打印所有试卷的科目信息
            for (Paper p : allPapers) {
                System.out.println("DEBUG [StudentExamManager]: 试卷='" + p.getPaperName() + "', 科目='" + p.getSubject() + "', 发布状态=" + p.getIsPublished());
            }
            
            List<Paper> filteredPapers;
            
            if ("全部".equals(subject)) {
                filteredPapers = allPapers;
                System.out.println("DEBUG [StudentExamManager]: 选择'全部'，不过滤，共 " + filteredPapers.size() + " 个试卷");
            } else {
                filteredPapers = new java.util.ArrayList<>();
                for (Paper p : allPapers) {
                    // 优化科目匹配逻辑，处理可能的空格和大小写问题
                    String paperSubject = p.getSubject() != null ? p.getSubject().trim() : "";
                    String filterSubject = subject != null ? subject.trim() : "";
                    boolean match = isSubjectMatch(paperSubject, filterSubject);
                    System.out.println("DEBUG [StudentExamManager]: 比较 试卷科目='" + paperSubject + "' vs 筛选科目='" + filterSubject + "', 匹配=" + match);
                    if (match) {
                        filteredPapers.add(p);
                    }
                }
                System.out.println("DEBUG [StudentExamManager]: 过滤后共 " + filteredPapers.size() + " 个试卷");
            }

            for (Paper p : filteredPapers) {
                // 统计各类型题目数量
                long singleCount = 0;
                long multipleCount = 0;
                long judgeCount = 0;
                long blankCount = 0;

                if (p.getQuestions() != null && !p.getQuestions().isEmpty()) {
                    singleCount = p.getQuestions().stream()
                            .filter(q -> q.getQuestionType() == com.exam.model.enums.QuestionType.SINGLE)
                            .count();
                    multipleCount = p.getQuestions().stream()
                            .filter(q -> q.getQuestionType() == com.exam.model.enums.QuestionType.MULTIPLE)
                            .count();
                    judgeCount = p.getQuestions().stream()
                            .filter(q -> q.getQuestionType() == com.exam.model.enums.QuestionType.JUDGE)
                            .count();
                    blankCount = p.getQuestions().stream()
                            .filter(q -> q.getQuestionType() == com.exam.model.enums.QuestionType.BLANK)
                            .count();
                }

                Object[] row = {
                        p.getPaperName(),
                        singleCount > 0 ? String.valueOf(singleCount) : "无",
                        multipleCount > 0 ? String.valueOf(multipleCount) : "无",
                        judgeCount > 0 ? String.valueOf(judgeCount) : "无",
                        blankCount > 0 ? String.valueOf(blankCount) : "无",
                        "开始考试"
                };
                tableModel.addRow(row);
                System.out.println("DEBUG [StudentExamManager]: 添加行到表格: " + p.getPaperName());
            }
            
            System.out.println("DEBUG [StudentExamManager]: 表格最终行数=" + tableModel.getRowCount());

            if (filteredPapers.isEmpty()) {
                System.out.println("DEBUG [StudentExamManager]: 没有匹配的试卷");
                // 当没有试卷时，不弹窗提示，只在表格中显示相应信息
                // 表格行将由UI层处理，此处不添加任何行
            }
        } catch (Exception e) {
            System.out.println("DEBUG [StudentExamManager]: 加载试卷失败: " + e.getMessage());
            e.printStackTrace();
            UIUtil.showError(parentComponent, "加载试卷失败：" + e.getMessage());
        }
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