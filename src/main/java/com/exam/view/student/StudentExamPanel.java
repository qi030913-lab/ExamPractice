package com.exam.view.student;

import com.exam.model.Paper;
import com.exam.model.User;
import com.exam.service.PaperService;
import com.exam.util.UIUtil;
import com.exam.util.IconUtil;
import com.exam.view.ExamFrame;
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
    private JTable paperTable;
    private DefaultTableModel tableModel;
    private String currentSubject = "全部";
    private static final String[] SUBJECTS = {"全部", "Java", "Vue", "数据结构", "马克思主义", "计算机网络", "操作系统", "数据库"};

    public StudentExamPanel(User student) {
        this.student = student;
        this.paperService = new PaperService();
        initComponents();
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
                currentSubject = subject;
                refreshSubjectButtons(categoryPanel);
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
        
        // 当前科目显示
        JLabel currentSubjectLabel = new JLabel("当前科目：" + currentSubject);
        currentSubjectLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        currentSubjectLabel.setForeground(new Color(100, 100, 100));
        titlePanel.add(currentSubjectLabel, BorderLayout.CENTER);
        
        // 刷新按钮
        JButton refreshButton = new JButton("刷新列表");
        refreshButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> loadPapersBySubject(currentSubject));
        titlePanel.add(refreshButton, BorderLayout.EAST);

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
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
        
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
        paperTable.getColumn("操作").setCellRenderer(new ButtonRenderer());
        paperTable.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // 表头样式
        paperTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        paperTable.getTableHeader().setBackground(new Color(245, 247, 250));
        paperTable.getTableHeader().setForeground(UIUtil.TEXT_COLOR);
        paperTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        paperTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        
        JScrollPane scrollPane = new JScrollPane(paperTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }

    /**
     * 根据科目加载试卷
     */
    private void loadPapersBySubject(String subject) {
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
                UIUtil.showInfo(this, "该科目暂无试卷");
            }
        } catch (Exception e) {
            UIUtil.showError(this, "加载试卷失败：" + e.getMessage());
        }
    }

    /**
     * 开始考试
     */
    private void startExam(int selectedRow) {
        if (selectedRow == -1) {
            UIUtil.showWarning(this, "请先选择要学习的试卷");
            return;
        }
        
        String paperName = (String) tableModel.getValueAt(selectedRow, 0);
        if (paperName == null || paperName.isEmpty()) {
            UIUtil.showWarning(this, "请选择有效的试卷");
            return;
        }
        
        if (!UIUtil.showConfirm(this, "确定要开始考试《" + paperName + "》吗？\n考试开始后将开始计时。")) {
            return;
        }
        
        try {
            Paper paper = paperService.getPaperByName(paperName);
            if (paper == null || paper.getQuestions().isEmpty()) {
                UIUtil.showError(this, "该试卷没有题目，无法考试");
                return;
            }
            
            // 打开考试界面
            com.exam.service.ExamService examService = new com.exam.service.ExamService();
            new ExamFrame(student, paper, examService).setVisible(true);
            
        } catch (Exception e) {
            UIUtil.showError(this, "开始考试失败：" + e.getMessage());
            e.printStackTrace();
        }
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

    // ========== 内部类：表格按钮渲染器和编辑器 ==========

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            setFont(new Font("微软雅黑", Font.PLAIN, 12));
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
            setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            setFocusPainted(false);
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            button.setFocusPainted(false);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                startExam(currentRow);
            }
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}
