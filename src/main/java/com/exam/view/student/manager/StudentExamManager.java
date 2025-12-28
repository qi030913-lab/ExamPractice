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
        if (tableModel == null) {
            return;
        }
        tableModel.setRowCount(0);
        try {
            List<Paper> allPapers = paperService.getAllPublishedPapers();
            List<Paper> filteredPapers;

            if ("全部".equals(subject)) {
                filteredPapers = allPapers;
            } else {
                filteredPapers = new java.util.ArrayList<>();
                for (Paper p : allPapers) {
                    if (subject.equals(p.getSubject())) {
                        filteredPapers.add(p);
                    }
                }
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
            }

            if (filteredPapers.isEmpty()) {
                // 当没有试卷时，不弹窗提示，只在表格中显示相应信息
                // 表格行将由UI层处理，此处不添加任何行
            }
        } catch (Exception e) {
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
}