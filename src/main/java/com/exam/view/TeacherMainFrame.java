package com.exam.view;

import com.exam.model.Question;
import com.exam.model.User;
import com.exam.model.enums.QuestionType;
import com.exam.model.enums.Difficulty;
import com.exam.service.QuestionService;
import com.exam.util.UIUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 教师主界面
 */
public class TeacherMainFrame extends JFrame {
    private final User teacher;
    private final QuestionService questionService;
    private JTable questionTable;
    private DefaultTableModel tableModel;

    public TeacherMainFrame(User teacher) {
        this.teacher = teacher;
        this.questionService = new QuestionService();
        initComponents();
        setTitle("在线考试系统 - 教师端");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UIUtil.centerWindow(this);
        loadQuestions();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // 顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIUtil.PRIMARY_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel welcomeLabel = new JLabel("欢迎，" + teacher.getRealName() + " 老师");
        welcomeLabel.setFont(UIUtil.HEADING_FONT);
        welcomeLabel.setForeground(Color.WHITE);
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JButton logoutButton = new JButton("退出登录");
        logoutButton.setFont(UIUtil.NORMAL_FONT);
        logoutButton.addActionListener(e -> logout());
        topPanel.add(logoutButton, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 中间标签页
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtil.NORMAL_FONT);
        
        tabbedPane.addTab("题库管理", createQuestionPanel());
        tabbedPane.addTab("试卷管理", createPaperPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = UIUtil.createSuccessButton("添加题目");
        addButton.addActionListener(e -> showAddQuestionDialog());
        
        JButton editButton = UIUtil.createPrimaryButton("编辑题目");
        editButton.addActionListener(e -> showEditQuestionDialog());
        
        JButton deleteButton = UIUtil.createDangerButton("删除题目");
        deleteButton.addActionListener(e -> deleteQuestion());
        
        JButton refreshButton = UIUtil.createPrimaryButton("刷新");
        refreshButton.addActionListener(e -> loadQuestions());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        
        // 表格
        String[] columns = {"ID", "类型", "科目", "题目内容", "正确答案", "分值", "难度"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        questionTable = new JTable(tableModel);
        questionTable.setFont(UIUtil.NORMAL_FONT);
        questionTable.getTableHeader().setFont(UIUtil.NORMAL_FONT);
        questionTable.setRowHeight(30);
        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(questionTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createPaperPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("试卷管理功能", SwingConstants.CENTER);
        label.setFont(UIUtil.HEADING_FONT);
        panel.add(label, BorderLayout.CENTER);
        
        JButton createPaperButton = UIUtil.createPrimaryButton("创建试卷");
        createPaperButton.addActionListener(e -> UIUtil.showInfo(this, "创建试卷功能待实现"));
        JPanel btnPanel = new JPanel();
        btnPanel.add(createPaperButton);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void loadQuestions() {
        tableModel.setRowCount(0);
        try {
            List<Question> questions = questionService.getAllQuestions();
            for (Question q : questions) {
                Object[] row = {
                    q.getQuestionId(),
                    q.getQuestionType().getDescription(),
                    q.getSubject(),
                    truncate(q.getContent(), 30),
                    q.getCorrectAnswer(),
                    q.getScore(),
                    q.getDifficulty() != null ? q.getDifficulty().getDescription() : ""
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            UIUtil.showError(this, "加载题目失败：" + e.getMessage());
        }
    }

    private void showAddQuestionDialog() {
        JDialog dialog = new JDialog(this, "添加题目", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 题目类型
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("题目类型:"), gbc);
        gbc.gridx = 1;
        JComboBox<QuestionType> typeCombo = new JComboBox<>(QuestionType.values());
        panel.add(typeCombo, gbc);
        
        // 科目
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("科目:"), gbc);
        gbc.gridx = 1;
        JTextField subjectField = new JTextField(20);
        panel.add(subjectField, gbc);
        
        // 题目内容
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("题目内容:"), gbc);
        gbc.gridx = 1;
        JTextArea contentArea = new JTextArea(3, 20);
        contentArea.setLineWrap(true);
        panel.add(new JScrollPane(contentArea), gbc);
        
        // 选项A
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("选项A:"), gbc);
        gbc.gridx = 1;
        JTextField optionAField = new JTextField(20);
        panel.add(optionAField, gbc);
        
        // 选项B
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("选项B:"), gbc);
        gbc.gridx = 1;
        JTextField optionBField = new JTextField(20);
        panel.add(optionBField, gbc);
        
        // 选项C
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("选项C:"), gbc);
        gbc.gridx = 1;
        JTextField optionCField = new JTextField(20);
        panel.add(optionCField, gbc);
        
        // 选项D
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("选项D:"), gbc);
        gbc.gridx = 1;
        JTextField optionDField = new JTextField(20);
        panel.add(optionDField, gbc);
        
        // 正确答案
        gbc.gridx = 0; gbc.gridy = 7;
        panel.add(new JLabel("正确答案:"), gbc);
        gbc.gridx = 1;
        JTextField answerField = new JTextField(20);
        panel.add(answerField, gbc);
        
        // 分值
        gbc.gridx = 0; gbc.gridy = 8;
        panel.add(new JLabel("分值:"), gbc);
        gbc.gridx = 1;
        JSpinner scoreSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        panel.add(scoreSpinner, gbc);
        
        // 难度
        gbc.gridx = 0; gbc.gridy = 9;
        panel.add(new JLabel("难度:"), gbc);
        gbc.gridx = 1;
        JComboBox<Difficulty> difficultyCombo = new JComboBox<>(Difficulty.values());
        panel.add(difficultyCombo, gbc);
        
        // 按钮
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        JButton saveButton = UIUtil.createSuccessButton("保存");
        JButton cancelButton = UIUtil.createDangerButton("取消");
        
        saveButton.addActionListener(e -> {
            try {
                Question question = new Question();
                question.setQuestionType((QuestionType) typeCombo.getSelectedItem());
                question.setSubject(subjectField.getText().trim());
                question.setContent(contentArea.getText().trim());
                question.setOptionA(optionAField.getText().trim());
                question.setOptionB(optionBField.getText().trim());
                question.setOptionC(optionCField.getText().trim());
                question.setOptionD(optionDField.getText().trim());
                question.setCorrectAnswer(answerField.getText().trim());
                question.setScore((Integer) scoreSpinner.getValue());
                question.setDifficulty((Difficulty) difficultyCombo.getSelectedItem());
                question.setCreatorId(teacher.getUserId());
                
                questionService.addQuestion(question);
                UIUtil.showInfo(dialog, "添加成功");
                dialog.dispose();
                loadQuestions();
            } catch (Exception ex) {
                UIUtil.showError(dialog, "添加失败：" + ex.getMessage());
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        btnPanel.add(saveButton);
        btnPanel.add(cancelButton);
        panel.add(btnPanel, gbc);
        
        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private void showEditQuestionDialog() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow == -1) {
            UIUtil.showWarning(this, "请先选择要编辑的题目");
            return;
        }
        
        Integer questionId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Question question = questionService.getQuestionById(questionId);
        
        if (question == null) {
            UIUtil.showError(this, "题目不存在");
            return;
        }
        
        // 编辑对话框（简化版，与添加类似）
        UIUtil.showInfo(this, "编辑功能待完善，题目ID: " + questionId);
    }

    private void deleteQuestion() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow == -1) {
            UIUtil.showWarning(this, "请先选择要删除的题目");
            return;
        }
        
        if (!UIUtil.showConfirm(this, "确定要删除这道题目吗？")) {
            return;
        }
        
        try {
            Integer questionId = (Integer) tableModel.getValueAt(selectedRow, 0);
            questionService.deleteQuestion(questionId);
            UIUtil.showInfo(this, "删除成功");
            loadQuestions();
        } catch (Exception e) {
            UIUtil.showError(this, "删除失败：" + e.getMessage());
        }
    }

    private void logout() {
        if (UIUtil.showConfirm(this, "确定要退出登录吗？")) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
