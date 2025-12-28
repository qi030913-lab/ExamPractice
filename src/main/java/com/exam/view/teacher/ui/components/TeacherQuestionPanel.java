package com.exam.view.teacher.ui.components;

import com.exam.model.Question;
import com.exam.model.enums.QuestionType;
import com.exam.model.enums.Difficulty;
import com.exam.service.QuestionService;
import com.exam.util.UIUtil;
import com.exam.view.teacher.TeacherUIHelper;
import com.exam.view.teacher.manager.QuestionManager;
import com.exam.view.teacher.ui.components.QuestionButtonEditor;
import com.exam.view.teacher.ui.components.QuestionButtonRenderer;
import com.exam.view.teacher.TeacherMainFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 教师端 - 题库管理面板
 */
public class TeacherQuestionPanel extends JPanel {
    private final TeacherQuestionCallback callback;
    private final QuestionService questionService;
    
    private JTable questionTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private JPanel tablePanel;
    
    // 回调接口
    public interface TeacherQuestionCallback {
        void onAddQuestion();
        void onEditQuestion(int row);
        void onDeleteQuestion(int row);
    }
    
    public TeacherQuestionPanel(QuestionService questionService, TeacherQuestionCallback callback) {
        this.questionService = questionService;
        this.callback = callback;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        // 右侧主内容区
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 标题区域
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel titleLabel = new JLabel("题库管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(UIUtil.TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        // 添加题目按钮放在右侧
        JButton addQuestionButton = TeacherUIHelper.createStyledButton("添加题目", UIUtil.SUCCESS_COLOR);
        addQuestionButton.addActionListener(e -> {
            if (callback != null) {
                callback.onAddQuestion();
            }
        });
        titlePanel.add(addQuestionButton, BorderLayout.EAST);

        contentPanel.add(titlePanel, BorderLayout.NORTH);

        // 表格面板
        tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));

        // 表格
        String[] columns = {"题目内容", "类型", "难度", "分值", "操作"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 操作列可编辑
                return column == 4;
            }
        };
        questionTable = new JTable(tableModel) {
            @Override
            public Class<?> getColumnClass(int column) {
                // 操作列使用JPanel类型
                if (column == 4) {
                    return JPanel.class;
                }
                return String.class;
            }
        };
        questionTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        questionTable.setRowHeight(50);
        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionTable.setGridColor(new Color(230, 230, 230));
        questionTable.setShowGrid(true);
        questionTable.setSelectionBackground(new Color(232, 240, 254));
        questionTable.setSelectionForeground(UIUtil.TEXT_COLOR);

        // 设置操作列渲染器
        questionTable.getColumnModel().getColumn(4).setCellRenderer(new QuestionButtonRenderer());
        questionTable.getColumnModel().getColumn(4).setCellEditor(new QuestionButtonEditor(questionTable, new QuestionButtonEditor.QuestionButtonCallback() {
            @Override
            public void onEdit(int row) {
                if (callback != null) {
                    callback.onEditQuestion(row);
                }
            }

            @Override
            public void onDelete(int row) {
                if (callback != null) {
                    callback.onDeleteQuestion(row);
                }
            }
        }));
        // 设置操作列宽度
        questionTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        questionTable.getColumnModel().getColumn(4).setMinWidth(120);

        // 表头样式
        questionTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        questionTable.getTableHeader().setBackground(new Color(245, 247, 250));
        questionTable.getTableHeader().setForeground(UIUtil.TEXT_COLOR);
        questionTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        questionTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        scrollPane = new JScrollPane(questionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // 加载数据
        loadQuestionsData();
    }
    
    /**
     * 加载题目数据
     */
    private void loadQuestionsData() {
        tableModel.setRowCount(0);
        try {
            List<Question> questions = questionService.getAllQuestions();
            for (Question q : questions) {
                Object[] row = {
                        TeacherUIHelper.truncate(q.getContent(), 50), // 截断长内容
                        q.getQuestionType().getDescription(),
                        q.getDifficulty().getDescription(),
                        q.getScore(),
                        "" // 操作列，由渲染器处理
                };
                tableModel.addRow(row);
            }
            
            // 检查是否有数据，如果没有则显示提示
            updateTableDisplay();
        } catch (Exception e) {
            UIUtil.showError(this, "加载题目失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateTableDisplay() {
        // 如果表格没有数据，显示"暂无题库"提示
        if (tableModel.getRowCount() == 0) {
            showNoDataMessage();
        } else {
            // 显示表头
            questionTable.getTableHeader().setVisible(true);
            // 确保显示表格
            showTable();
        }
    }
    
    private void showNoDataMessage() {
        // 隐藏表格组件
        questionTable.setVisible(false);
        
        // 创建"暂无题库"提示标签
        JLabel noDataLabel = new JLabel("暂无题库", SwingConstants.CENTER);
        noDataLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        noDataLabel.setForeground(new Color(150, 150, 150));
        noDataLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // 获取表格所在的面板并替换为提示标签
        Container viewport = questionTable.getParent();
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
        questionTable.setVisible(true);
        
        // 恢复表格显示
        Container viewport = questionTable.getParent();
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
     * 刷新数据
     */
    public void refreshData() {
        loadQuestionsData();
    }
    
    /**
     * 获取表格模型（供主框架使用）
     */
    public DefaultTableModel getTableModel() {
        return tableModel;
    }
}