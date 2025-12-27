package com.exam.view.teacher;

import com.exam.model.Question;
import com.exam.service.QuestionService;
import com.exam.util.UIUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 教师端 - 题库管理面板
 */
public class TeacherQuestionPanel extends JPanel {
    private final TeacherQuestionCallback callback;
    private final QuestionService questionService;
    
    private JTable questionTable;
    private DefaultTableModel tableModel;
    private String currentSubject = "全部";
    
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

        // 左侧科目分类栏
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
        for (String subject : TeacherConstants.SUBJECTS) {
            JButton subjectButton = TeacherUIHelper.createSubjectButton(subject, subject.equals(currentSubject));
            subjectButton.addActionListener(e -> {
                currentSubject = subject;
                TeacherUIHelper.refreshSubjectButtons(categoryPanel, currentSubject);
                loadQuestionsBySubject(subject);
            });
            categoryPanel.add(subjectButton);
        }

        categoryPanel.add(Box.createVerticalGlue());

        add(categoryPanel, BorderLayout.WEST);

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
        JButton addButton = TeacherUIHelper.createStyledButton("添加题目", UIUtil.SUCCESS_COLOR);
        addButton.addActionListener(e -> {
            if (callback != null) {
                callback.onAddQuestion();
            }
        });
        titlePanel.add(addButton, BorderLayout.EAST);

        contentPanel.add(titlePanel, BorderLayout.NORTH);

        // 表格面板
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));

        // 表格
        String[] columns = {"科目", "类型", "题目内容", "正确答案", "操作"};
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

        // 表头样式
        questionTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        questionTable.getTableHeader().setBackground(new Color(245, 247, 250));
        questionTable.getTableHeader().setForeground(UIUtil.TEXT_COLOR);
        questionTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        questionTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        JScrollPane scrollPane = new JScrollPane(questionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // 加载数据
        loadQuestionsBySubject(currentSubject);
    }
    
    /**
     * 根据科目加载题目
     */
    private void loadQuestionsBySubject(String subject) {
        tableModel.setRowCount(0);
        try {
            List<Question> allQuestions = questionService.getAllQuestions();
            List<Question> filteredQuestions;

            if ("全部".equals(subject)) {
                filteredQuestions = allQuestions;
            } else {
                filteredQuestions = new ArrayList<>();
                for (Question q : allQuestions) {
                    if (subject.equals(q.getSubject())) {
                        filteredQuestions.add(q);
                    }
                }
            }

            for (Question q : filteredQuestions) {
                Object[] row = {
                        q.getSubject(),
                        q.getQuestionType().getDescription(),
                        TeacherUIHelper.truncate(q.getContent(), 50),
                        q.getCorrectAnswer(),
                        "" // 操作列，由渲染器处理
                };
                tableModel.addRow(row);
            }

        } catch (Exception e) {
            UIUtil.showError(this, "加载题目失败：" + e.getMessage());
        }
    }
    
    /**
     * 刷新数据
     */
    public void refreshData() {
        loadQuestionsBySubject(currentSubject);
    }
    
    /**
     * 获取表格模型（供主框架使用）
     */
    public DefaultTableModel getTableModel() {
        return tableModel;
    }
    
    /**
     * 获取当前科目
     */
    public String getCurrentSubject() {
        return currentSubject;
    }
}
