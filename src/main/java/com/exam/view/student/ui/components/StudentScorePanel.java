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
    // 缓存考试记录，避免点击按钮时重复查询
    private List<ExamRecord> cachedRecords = new java.util.ArrayList<>();
    
    // 分页相关
    private int currentPage = 1;
    private int totalPages = 0;
    private JButton firstPageBtn;
    private JButton prevPageBtn;
    private JButton nextPageBtn;
    private JButton lastPageBtn;
    private JLabel pageInfoLabel;

    public StudentScorePanel(User student) {
        this.student = student;
        this.examService = new ExamService();
        this.scoreManager = new StudentScoreManager(student);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 标题区域
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);

        // 成绩记录表格区域 - 放在CENTER使其自动填充剩余空间
        tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // 分页控件区域 - 固定在底部
        JPanel paginationPanel = createPaginationPanel();
        add(paginationPanel, BorderLayout.SOUTH);
        
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
        JPanel tablePanel = new JPanel(new BorderLayout(0, 0));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
        
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
        scoreTable.setRowHeight(43);
        scoreTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scoreTable.setGridColor(new Color(230, 230, 230));
        scoreTable.setShowGrid(true);
        scoreTable.setSelectionBackground(new Color(232, 240, 254));
        scoreTable.setSelectionForeground(UIUtil.TEXT_COLOR);
        
        // 为详情列设置按钮渲染器和编辑器
        scoreTable.getColumn("详情").setCellRenderer(new StudentTableButtonRenderer());
        scoreTable.getColumn("详情").setCellEditor(new StudentTableButtonEditor(new JCheckBox(), row -> {
            try {
                // 使用缓存的考试记录，避免重复查询
                if (row < cachedRecords.size()) {
                    showExamDetail(cachedRecords.get(row).getRecordId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        
        // 表头样式
        scoreTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        scoreTable.getTableHeader().setBackground(new Color(245, 247, 250));
        scoreTable.getTableHeader().setForeground(UIUtil.TEXT_COLOR);
        scoreTable.getTableHeader().setPreferredSize(new Dimension(0, 35));
        scoreTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        
        scrollPane = new JScrollPane(scoreTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    /**
     * 创建分页控件面板
     */
    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 30, 10, 30));
        
        // 首页按钮
        firstPageBtn = new JButton("首页");
        firstPageBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        firstPageBtn.setFocusPainted(false);
        firstPageBtn.addActionListener(e -> goToPage(1));
        
        // 上一页按钮
        prevPageBtn = new JButton("上一页");
        prevPageBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        prevPageBtn.setFocusPainted(false);
        prevPageBtn.addActionListener(e -> goToPage(currentPage - 1));
        
        // 页码信息标签
        pageInfoLabel = new JLabel("第 1 页 / 共 1 页");
        pageInfoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        pageInfoLabel.setForeground(UIUtil.TEXT_COLOR);
        
        // 下一页按钮
        nextPageBtn = new JButton("下一页");
        nextPageBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        nextPageBtn.setFocusPainted(false);
        nextPageBtn.addActionListener(e -> goToPage(currentPage + 1));
        
        // 末页按钮
        lastPageBtn = new JButton("末页");
        lastPageBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        lastPageBtn.setFocusPainted(false);
        lastPageBtn.addActionListener(e -> goToPage(totalPages));
        
        panel.add(firstPageBtn);
        panel.add(prevPageBtn);
        panel.add(pageInfoLabel);
        panel.add(nextPageBtn);
        panel.add(lastPageBtn);
        
        return panel;
    }
    
    /**
     * 跳转到指定页
     */
    private void goToPage(int page) {
        if (page < 1 || page > totalPages || page == currentPage) {
            return;
        }
        currentPage = page;
        loadScores();
    }
    
    /**
     * 更新分页按钮状态
     */
    private void updatePaginationButtons() {
        firstPageBtn.setEnabled(currentPage > 1);
        prevPageBtn.setEnabled(currentPage > 1);
        nextPageBtn.setEnabled(currentPage < totalPages);
        lastPageBtn.setEnabled(currentPage < totalPages);
        
        if (totalPages > 0) {
            pageInfoLabel.setText("第 " + currentPage + " 页 / 共 " + totalPages + " 页");
        } else {
            pageInfoLabel.setText("暂无数据");
        }
    }

    /**
     * 加载成绩数据
     */
    private void loadScores() {
        // 清空现有数据
        scoreTableModel.setRowCount(0);
        // 显示加载中提示
        showLoadingMessage();
        
        // 先获取总页数
        totalPages = scoreManager.getTotalPages();
        
        // 如果没有数据，直接显示无数据提示
        if (totalPages == 0) {
            currentPage = 1;
            updatePaginationButtons();
            showNoDataMessage();
            return;
        }
        
        // 确保当前页码在有效范围内
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        
        // 加载数据并缓存考试记录，完成后更新界面
        scoreManager.loadScoresPaginated(scoreTableModel, this, cachedRecords, () -> {
            // 数据加载完成后检查是否有数据
            updateTableHeaderVisibility();
            // 更新分页按钮状态
            updatePaginationButtons();
        }, currentPage);
    }
    
    /**
     * 显示加载中提示
     */
    private void showLoadingMessage() {
        JLabel loadingLabel = new JLabel("加载中...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        loadingLabel.setForeground(new Color(100, 100, 100));
        
        tablePanel.removeAll();
        tablePanel.add(loadingLabel, BorderLayout.CENTER);
        tablePanel.revalidate();
        tablePanel.repaint();
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
        // 创建"暂无考试记录"提示标签
        JLabel noDataLabel = new JLabel("暂无考试记录", SwingConstants.CENTER);
        noDataLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        noDataLabel.setForeground(new Color(150, 150, 150));
        noDataLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        tablePanel.removeAll();
        tablePanel.add(noDataLabel, BorderLayout.CENTER);
        tablePanel.revalidate();
        tablePanel.repaint();
    }
    
    private void showTable() {
        // 确保表格可见
        scoreTable.setVisible(true);
        
        tablePanel.removeAll();
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // 强制重新验证和重绘
        tablePanel.revalidate();
        tablePanel.repaint();
        
        // 同时对表格组件进行重绘以确保显示更新
        scoreTable.revalidate();
        scoreTable.repaint();
    }

    /**
     * 显示考试详情对话框
     */
    private void showExamDetail(int recordId) {
        scoreManager.showExamDetail(recordId, this);
    }
}