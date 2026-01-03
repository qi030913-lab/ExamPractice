package com.exam.view.student.ui.components;

import com.exam.model.Paper;
import com.exam.model.User;
import com.exam.service.PaperService;
import com.exam.util.UIUtil;
import com.exam.util.IconUtil;
import com.exam.view.student.StudentMainFrame;
import com.exam.view.student.manager.StudentExamManager;
import com.exam.view.student.ui.components.StudentTableButtonEditor;
import com.exam.view.student.ui.components.StudentTableButtonRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 学生端 - 考试列表面板
 * 
 * 功能说明：
 * 1. 按科目分类展示试卷列表
 * 2. 显示试卷的题型统计信息
 * 3. 开始考试功能
 * 4. 刷新试卷列表
 * 
 * @author 系统管理员
 * @version 1.0
 */
public class StudentExamPanel extends JPanel {
    private final User student;
    private final PaperService paperService;
    private final StudentExamManager examManager;
    private JTable paperTable;
    private DefaultTableModel tableModel;
    private String currentSubject = "全部";
    private StudentMainFrame mainFrame; // 添加主框架引用
    private JLabel currentSubjectLabel; // 添加对科目标签的引用
    private static final String[] SUBJECTS = {"全部", "Java", "Vue", "数据结构", "马克思主义", "计算机网络", "操作系统", "数据库", "英语", "品牌与营销"};
    
    // 保存关键组件的引用，避免组件层次结构被破坏后无法恢复
    private JScrollPane tableScrollPane;
    private JPanel tableContainerPanel;
    private JLabel noDataLabel;
    
    public StudentExamPanel(User student) {
        this(student, "全部", null);
    }
    
    public StudentExamPanel(User student, String defaultSubject) {
        this(student, defaultSubject, null);
    }
    
    public StudentExamPanel(User student, String defaultSubject, StudentMainFrame mainFrame) {
        this.student = student;
        this.paperService = new PaperService();
        this.examManager = new StudentExamManager(student);
        this.currentSubject = defaultSubject;
        this.mainFrame = mainFrame;
        initComponents();
    }

    /**
     * 刷新试卷列表
     */
    public void refreshExamList() {
        // 清除缓存以确保获取最新数据
        StudentExamManager.clearCache();
        loadPapersBySubject(currentSubject);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        // 左侧科目分类栏
        JPanel categoryPanel = createCategoryPanel();
        add(categoryPanel, BorderLayout.WEST);

        // 右侧主内容区
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);

        // 初始化表格数据
        loadPapersBySubject(currentSubject);
    }

    /**
     * 创建科目分类面板
     */
    private JPanel createCategoryPanel() {
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBackground(new Color(250, 250, 250));
        categoryPanel.setPreferredSize(new Dimension(180, 0));
        categoryPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        // 分类标题
        JPanel categoryTitlePanel = new JPanel(new BorderLayout());
        categoryTitlePanel.setBackground(new Color(250, 250, 250));
        categoryTitlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        categoryTitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JLabel categoryTitleLabel = new JLabel("科目分类");
        categoryTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        categoryTitleLabel.setForeground(UIUtil.TEXT_COLOR);
        categoryTitlePanel.add(categoryTitleLabel, BorderLayout.WEST);
        
        categoryPanel.add(categoryTitlePanel);
        
        // 分隔线
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setForeground(new Color(230, 230, 230));
        categoryPanel.add(separator);
        
        // 科目列表
        for (String subject : SUBJECTS) {
            JButton subjectButton = createSubjectButton(subject, subject.equals(currentSubject));
            subjectButton.addActionListener(e -> {
                System.out.println("DEBUG [StudentExamPanel]: 科目按钮被点击, 科目=" + subject);
                currentSubject = subject;
                // 更新全局当前考试科目
                if (mainFrame != null) {
                    mainFrame.setCurrentExamSubject(subject);
                }
                refreshSubjectButtons(categoryPanel);
                // 更新界面上的科目标签
                updateCurrentSubjectLabel();
                loadPapersBySubject(subject);
            });
            categoryPanel.add(subjectButton);
        }
        
        categoryPanel.add(Box.createVerticalGlue());
        
        return categoryPanel;
    }

    /**
     * 创建内容面板
     */
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 标题区域
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel titleLabel = new JLabel("考试列表");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(UIUtil.TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // 当前科目显示（保留但不显示）
        currentSubjectLabel = new JLabel("当前科目：" + currentSubject);
        currentSubjectLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        currentSubjectLabel.setForeground(new Color(100, 100, 100));
        // titlePanel.add(currentSubjectLabel, BorderLayout.CENTER); // 不显示

        contentPanel.add(titlePanel, BorderLayout.NORTH);

        // 考试记录表格区域
        JPanel tablePanel = createTablePanel();
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        
        return contentPanel;
    }

    /**
     * 创建表格面板
     */
    private JPanel createTablePanel() {
        tableContainerPanel = new JPanel(new BorderLayout(0, 15));
        tableContainerPanel.setBackground(Color.WHITE);
        tableContainerPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
        
        // 试卷列表表格
        String[] columns = {"名称", "单选", "多选", "判断", "填空", "操作"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };
        paperTable = new JTable(tableModel);
        paperTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        paperTable.setRowHeight(45);
        paperTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paperTable.setGridColor(new Color(230, 230, 230));
        paperTable.setShowGrid(true);
        paperTable.setSelectionBackground(new Color(232, 240, 254));
        paperTable.setSelectionForeground(UIUtil.TEXT_COLOR);
        
        // 为操作列设置按钮渲染器和编辑器
        paperTable.getColumn("操作").setCellRenderer(new StudentTableButtonRenderer());
        paperTable.getColumn("操作").setCellEditor(new StudentTableButtonEditor(new JCheckBox(), row -> startExam(row)));
        
        // 表头样式
        paperTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        paperTable.getTableHeader().setBackground(new Color(245, 247, 250));
        paperTable.getTableHeader().setForeground(UIUtil.TEXT_COLOR);
        paperTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        paperTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        
        tableScrollPane = new JScrollPane(paperTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        
        // 创建"暂无数据"提示标签
        noDataLabel = new JLabel("暂无该科目考试", SwingConstants.CENTER);
        noDataLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        noDataLabel.setForeground(new Color(150, 150, 150));
        noDataLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        tableContainerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        System.out.println("DEBUG [StudentExamPanel]: createTablePanel() 完成, tableContainerPanel=" + tableContainerPanel + ", tableScrollPane=" + tableScrollPane);
        
        return tableContainerPanel;
    }

    /**
     * 根据科目加载试卷
     */
    private void loadPapersBySubject(String subject) {
        // 清空现有数据并显示加载提示
        tableModel.setRowCount(0);
        showLoadingMessage();
        
        // 异步加载数据
        examManager.loadPapersBySubject(subject, tableModel, this);
        
        // 延迟检查表格状态（等待异步加载完成）
        javax.swing.Timer timer = new javax.swing.Timer(100, e -> {
            updateTableHeaderVisibility();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * 显示加载中提示
     */
    private void showLoadingMessage() {
        if (tableContainerPanel == null) return;
        
        JLabel loadingLabel = new JLabel("加载中...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        loadingLabel.setForeground(new Color(100, 100, 100));
        
        tableContainerPanel.removeAll();
        tableContainerPanel.add(loadingLabel, BorderLayout.CENTER);
        tableContainerPanel.revalidate();
        tableContainerPanel.repaint();
    }
    
    private void updateTableHeaderVisibility() {
        // 如果表格没有数据，显示"暂无考试记录"提示
        if (tableModel.getRowCount() == 0) {
            showNoDataMessage();
        } else {
            // 显示表头
            paperTable.getTableHeader().setVisible(true);
            // 确保显示表格
            showTable();
        }
    }
    
    private void showNoDataMessage() {
        if (tableContainerPanel == null || noDataLabel == null) {
            return;
        }
        
        // 移除所有组件，然后添加无数据提示
        tableContainerPanel.removeAll();
        tableContainerPanel.add(noDataLabel, BorderLayout.CENTER);
        
        tableContainerPanel.revalidate();
        tableContainerPanel.repaint();
    }
    
    private void showTable() {
        if (tableContainerPanel == null || tableScrollPane == null) {
            return;
        }
        
        // 移除所有组件，然后添加表格
        tableContainerPanel.removeAll();
        tableContainerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // 确保表格可见
        paperTable.setVisible(true);
        
        // 强制重新验证和重绘
        tableContainerPanel.revalidate();
        tableContainerPanel.repaint();
        
        // 同时对表格组件进行重绘以确保显示更新
        paperTable.revalidate();
        paperTable.repaint();
    }

    /**
     * 开始考试
     */
    private void startExam(int selectedRow) {
        examManager.startExam(selectedRow, tableModel, paperTable);
    }

    // ========== 辅助方法 ==========

    private JButton createSubjectButton(String subject, boolean isActive) {
        JButton button = new JButton(subject);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        
        Icon icon = IconUtil.createCircleIcon(
            isActive ? UIUtil.PRIMARY_COLOR : new Color(150, 150, 150), 8);
        button.setIcon(icon);
        button.setIconTextGap(12);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
        
        updateSubjectButtonStyle(button, isActive);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!button.getBackground().equals(new Color(232, 240, 254))) {
                    button.setBackground(new Color(245, 245, 245));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!button.getBackground().equals(new Color(232, 240, 254))) {
                    button.setBackground(new Color(250, 250, 250));
                }
            }
        });
        
        return button;
    }

    private void updateSubjectButtonStyle(JButton button, boolean isActive) {
        if (isActive) {
            button.setBackground(new Color(232, 240, 254));
            button.setForeground(UIUtil.PRIMARY_COLOR);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, UIUtil.PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(12, 17, 12, 10)
            ));
            Icon icon = IconUtil.createCircleIcon(UIUtil.PRIMARY_COLOR, 8);
            button.setIcon(icon);
        } else {
            button.setBackground(new Color(250, 250, 250));
            button.setForeground(new Color(80, 80, 80));
            button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
            Icon icon = IconUtil.createCircleIcon(new Color(150, 150, 150), 8);
            button.setIcon(icon);
        }
    }

    private void refreshSubjectButtons(JPanel categoryPanel) {
        Component[] components = categoryPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                String buttonText = button.getText();
                boolean isActive = buttonText.equals(currentSubject);
                updateSubjectButtonStyle(button, isActive);
            }
        }
    }

    /**
     * 更新界面上显示的当前科目标签
     */
    private void updateCurrentSubjectLabel() {
        currentSubjectLabel.setText("当前科目：" + currentSubject);
    }

    /**
     * 获取父级主框架
     */
    private StudentMainFrame getParentMainFrame() {
        return mainFrame;
    }
}