package com.exam.view.student.ui.components;

import com.exam.model.User;
import com.exam.model.ExamRecord;
import com.exam.model.AnswerRecord;
import com.exam.service.ExamService;
import com.exam.util.UIUtil;
import com.exam.view.student.manager.StudentScoreManager;
import com.exam.view.student.ui.components.StudentTableButtonEditor;
import com.exam.view.student.ui.components.StudentTableButtonRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.List;

/**
 * 学生端 - 成绩查询面板
 * 
 * 功能说明：
 * 1. 展示学生的所有考试记录
 * 2. 显示详细的成绩信息（总分、得分、正确题数、错误题数、耗时等）
 * 3. 查看考试详情（包括错题分析）
 * 
 * @author 系统管理员
 * @version 1.0
 */
public class StudentScorePanel extends JPanel {
    private final User student;
    private final ExamService examService;
    private final StudentScoreManager scoreManager;
    private DefaultTableModel scoreTableModel;
    private JTable scoreTable;
    private JScrollPane scrollPane;
    private JPanel tablePanel;

    public StudentScorePanel(User student) {
        this.student = student;
        this.examService = new ExamService();
        this.scoreManager = new StudentScoreManager(student);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 标题区域
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);

        // 成绩记录表格区域
        tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // 加载成绩数据
        loadScores();
    }

    /**
     * 创建标题面板
     */
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel titleLabel = new JLabel("成绩查询");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(UIUtil.TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        return titlePanel;
    }

    /**
     * 创建表格面板
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
        
        // 成绩表格
        String[] columns = {"试卷名称", "总分", "得分", "正确题数", "错误题数", "考试时间", "耗时", "详情"};
        scoreTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };
        scoreTable = new JTable(scoreTableModel);
        scoreTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        scoreTable.setRowHeight(45);
        scoreTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scoreTable.setGridColor(new Color(230, 230, 230));
        scoreTable.setShowGrid(true);
        scoreTable.setSelectionBackground(new Color(232, 240, 254));
        scoreTable.setSelectionForeground(UIUtil.TEXT_COLOR);
        
        // 为详情列设置按钮渲染器和编辑器
        scoreTable.getColumn("详情").setCellRenderer(new StudentTableButtonRenderer());
        scoreTable.getColumn("详情").setCellEditor(new StudentTableButtonEditor(new JCheckBox(), row -> {
            try {
                List<ExamRecord> records = examService.getStudentExamRecords(student.getUserId());
                if (row < records.size()) {
                    showExamDetail(records.get(row).getRecordId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        
        // 表头样式
        scoreTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        scoreTable.getTableHeader().setBackground(new Color(245, 247, 250));
        scoreTable.getTableHeader().setForeground(UIUtil.TEXT_COLOR);
        scoreTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        scoreTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        
        scrollPane = new JScrollPane(scoreTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }

    /**
     * 加载成绩数据
     */
    private void loadScores() {
        // 清空现有数据
        scoreTableModel.setRowCount(0);
        scoreManager.loadScores(scoreTableModel, this);
        // 检查是否有数据，如果没有则显示提示
        updateTableHeaderVisibility();
    }
    
    private void updateTableHeaderVisibility() {
        // 如果表格没有数据，显示"暂无考试记录"提示
        if (scoreTableModel.getRowCount() == 0) {
            showNoDataMessage();
        } else {
            // 显示表头
            scoreTable.getTableHeader().setVisible(true);
            // 确保显示表格
            showTable();
        }
    }
    
    private void showNoDataMessage() {
        // 隐藏表格组件
        scoreTable.setVisible(false);
        
        // 创建"暂无考试记录"提示标签
        JLabel noDataLabel = new JLabel("暂无考试记录", SwingConstants.CENTER);
        noDataLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        noDataLabel.setForeground(new Color(150, 150, 150));
        noDataLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // 获取表格所在的面板并替换为提示标签
        Container viewport = scoreTable.getParent();
        if (viewport == null) return;
        
        Container scrollPane = viewport.getParent();
        if (scrollPane == null) return;
        
        JPanel tablePanel = (JPanel) scrollPane.getParent();
        if (tablePanel == null) return;

        tablePanel.removeAll();
        tablePanel.setLayout(new BorderLayout(0, 15));
        tablePanel.add(noDataLabel, BorderLayout.CENTER);
        
        tablePanel.revalidate();
        tablePanel.repaint();
    }
    
    private void showTable() {
        // 确保表格可见
        scoreTable.setVisible(true);
        
        // 恢复表格显示
        Container viewport = scoreTable.getParent();
        if (viewport == null) return;
        
        Container scrollPane = viewport.getParent();
        if (scrollPane == null) return;
        
        JPanel tablePanel = (JPanel) scrollPane.getParent();
        if (tablePanel == null) return;
        
        // 检查当前是否显示的是提示标签，如果是则需要重新设置
        if (tablePanel.getComponentCount() == 0 || !(tablePanel.getComponent(0) instanceof JLabel)) {
            tablePanel.removeAll();
            tablePanel.setLayout(new BorderLayout(0, 15));
            tablePanel.add(scrollPane, BorderLayout.CENTER);
        }
        
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    /**
     * 显示考试详情对话框
     */
    private void showExamDetail(int recordId) {
        scoreManager.showExamDetail(recordId, this);
    }
}