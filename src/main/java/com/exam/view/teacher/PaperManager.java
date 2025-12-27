package com.exam.view.teacher;

import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.service.PaperService;
import com.exam.util.UIUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 试卷管理器 - 处理所有与试卷相关的操作
 */
public class PaperManager {
    private final PaperService paperService;
    private final TeacherMainFrame mainFrame;
    
    public PaperManager(PaperService paperService, TeacherMainFrame mainFrame) {
        this.paperService = paperService;
        this.mainFrame = mainFrame;
    }
    
    /**
     * 显示添加试卷对话框
     */
    public void showAddPaperDialog() {
        JDialog dialog = new JDialog(mainFrame, "创建试卷", true);
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(new Color(245, 250, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 标题
        JLabel titleLabel = new JLabel("创建新试卷");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 表单
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 试卷名称
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel nameLabel = new JLabel("试卷名称：");
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField paperNameField = new JTextField(20);
        paperNameField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(paperNameField, gbc);

        // 科目
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel subjectLabel = new JLabel("科　　目：");
        subjectLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(subjectLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        String[] subjectOptions = TeacherConstants.getSubjectsWithoutAll();
        JComboBox<String> subjectCombo = new JComboBox<>(subjectOptions);
        subjectCombo.setEditable(true);
        subjectCombo.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(subjectCombo, gbc);

        // 考试时长
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel durationLabel = new JLabel("时长(分钟)：");
        durationLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(durationLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(90, 10, 300, 10));
        durationSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(durationSpinner, gbc);

        // 及格分数
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0;
        JLabel passScoreLabel = new JLabel("及格分数：");
        passScoreLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(passScoreLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner passScoreSpinner = new JSpinner(new SpinnerNumberModel(60, 0, 100, 5));
        passScoreSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(passScoreSpinner, gbc);

        // 描述
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0;
        JLabel descLabel = new JLabel("描　　述：");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(descLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        formPanel.add(descScroll, gbc);

        // 选择题目
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.weightx = 0;
        JLabel questionsLabel = new JLabel("选择题目：");
        questionsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(questionsLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JButton selectQuestionsButton = new JButton("选择题目");
        selectQuestionsButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        selectQuestionsButton.setBackground(UIUtil.PRIMARY_COLOR);
        selectQuestionsButton.setForeground(Color.BLACK);
        selectQuestionsButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        selectQuestionsButton.setFocusPainted(false);

        // 用于存储选中的题目ID
        java.util.List<Integer> selectedQuestionIds = new java.util.ArrayList<>();
        JLabel selectedCountLabel = new JLabel("已选 0 道题目");
        selectedCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        selectedCountLabel.setForeground(new Color(100, 100, 100));

        selectQuestionsButton.addActionListener(e -> {
            showQuestionSelectionDialog(dialog, selectedQuestionIds, selectedCountLabel);
        });

        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        selectPanel.setBackground(Color.WHITE);
        selectPanel.add(selectQuestionsButton);
        selectPanel.add(selectedCountLabel);
        formPanel.add(selectPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(245, 250, 255));

        JButton confirmButton = TeacherUIHelper.createStyledButton("创建试卷", UIUtil.PRIMARY_COLOR);
        confirmButton.addActionListener(e -> {
            String paperName = paperNameField.getText().trim();
            String subject = subjectCombo.getSelectedItem() != null
                    ? subjectCombo.getSelectedItem().toString().trim()
                    : "";

            if (paperName.isEmpty()) {
                UIUtil.showWarning(dialog, "试卷名称不能为空");
                return;
            }
            if (subject.isEmpty()) {
                UIUtil.showWarning(dialog, "科目不能为空");
                return;
            }
            if (selectedQuestionIds.isEmpty()) {
                UIUtil.showWarning(dialog, "请至少选择一道题目");
                return;
            }

            try {
                Paper paper = new Paper();
                paper.setPaperName(paperName);
                paper.setSubject(subject);
                paper.setDuration((Integer) durationSpinner.getValue());
                paper.setPassScore((Integer) passScoreSpinner.getValue());
                paper.setDescription(descArea.getText().trim());
                paper.setCreatorId(mainFrame.getTeacher().getUserId());

                int paperId = paperService.createPaper(paper, selectedQuestionIds);

                UIUtil.showInfo(dialog, "试卷创建成功！\n题目数：" + selectedQuestionIds.size() + " 道");
                dialog.dispose();
                // 通知主框架刷新数据
                mainFrame.refreshPaperData();

            } catch (Exception ex) {
                UIUtil.showError(dialog, "创建试卷失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        });

        JButton cancelButton = TeacherUIHelper.createStyledButton("取消", new Color(120, 144, 156));
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    /**
     * 显示题目选择对话框
     */
    private void showQuestionSelectionDialog(JDialog parentDialog, java.util.List<Integer> selectedQuestionIds, JLabel selectedCountLabel) {
        JDialog dialog = new JDialog(parentDialog, "选择题目", true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(parentDialog);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 标题
        JLabel titleLabel = new JLabel("选择试卷题目");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 题目列表
        String[] columns = {"选择", "题目ID", "科目", "类型", "题目内容", "分值"};
        DefaultTableModel questionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 只有选择列可编辑
            }
        };

        JTable questionSelectTable = new JTable(questionTableModel);
        questionSelectTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        questionSelectTable.setRowHeight(40);
        questionSelectTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        questionSelectTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        questionSelectTable.getTableHeader().setBackground(new Color(245, 247, 250));

        // 加载所有题目
        try {
            List<Question> allQuestions = mainFrame.getQuestionService().getAllQuestions();
            for (Question q : allQuestions) {
                boolean isSelected = selectedQuestionIds.contains(q.getQuestionId());
                Object[] row = {
                        isSelected,
                        q.getQuestionId(),
                        q.getSubject(),
                        q.getQuestionType().getDescription(),
                        TeacherUIHelper.truncate(q.getContent(), 40),
                        q.getScore()
                };
                questionTableModel.addRow(row);
            }
        } catch (Exception e) {
            UIUtil.showError(dialog, "加载题目失败：" + e.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(questionSelectTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton confirmButton = TeacherUIHelper.createStyledButton("确定", UIUtil.PRIMARY_COLOR);
        confirmButton.addActionListener(e -> {
            selectedQuestionIds.clear();
            for (int i = 0; i < questionTableModel.getRowCount(); i++) {
                Boolean isSelected = (Boolean) questionTableModel.getValueAt(i, 0);
                if (isSelected != null && isSelected) {
                    String questionIdStr = questionTableModel.getValueAt(i, 1).toString();
                    selectedQuestionIds.add(Integer.parseInt(questionIdStr));
                }
            }
            selectedCountLabel.setText("已选 " + selectedQuestionIds.size() + " 道题目");
            dialog.dispose();
        });

        JButton cancelButton = TeacherUIHelper.createStyledButton("取消", new Color(120, 144, 156));
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    /**
     * 显示编辑试卷对话框
     */
    public void showEditPaperDialog(Paper paper) {
        JDialog dialog = new JDialog(mainFrame, "编辑试卷", true);
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(new Color(245, 250, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 标题
        JLabel titleLabel = new JLabel("编辑试卷");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 表单
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 试卷名称
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel nameLabel = new JLabel("试卷名称：");
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField paperNameField = new JTextField(20);
        paperNameField.setText(paper.getPaperName());
        paperNameField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(paperNameField, gbc);

        // 科目
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel subjectLabel = new JLabel("科　　目：");
        subjectLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(subjectLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        String[] subjectOptions = TeacherConstants.getSubjectsWithoutAll();
        JComboBox<String> subjectCombo = new JComboBox<>(subjectOptions);
        subjectCombo.setSelectedItem(paper.getSubject());
        subjectCombo.setEditable(true);
        subjectCombo.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(subjectCombo, gbc);

        // 考试时长
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel durationLabel = new JLabel("时长(分钟)：");
        durationLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(durationLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(paper.getDuration().intValue(), 10, 300, 10));
        durationSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(durationSpinner, gbc);

        // 及格分数
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0;
        JLabel passScoreLabel = new JLabel("及格分数：");
        passScoreLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(passScoreLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner passScoreSpinner = new JSpinner(new SpinnerNumberModel(paper.getPassScore().intValue(), 0, 100, 5));
        passScoreSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(passScoreSpinner, gbc);

        // 描述
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0;
        JLabel descLabel = new JLabel("描　　述：");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(descLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setText(paper.getDescription());
        descArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        formPanel.add(descScroll, gbc);

        // 题目信息（显示，不可修改）
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.weightx = 0;
        JLabel questionsLabel = new JLabel("题目数量：");
        questionsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(questionsLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        int questionCount = paper.getQuestions() != null ? paper.getQuestions().size() : 0;
        JLabel countLabel = new JLabel(questionCount + " 道题目（总分：" + paper.getTotalScore() + "分）");
        countLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        countLabel.setForeground(new Color(100, 100, 100));
        formPanel.add(countLabel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(245, 250, 255));

        JButton saveButton = TeacherUIHelper.createStyledButton("保存修改", UIUtil.PRIMARY_COLOR);
        saveButton.addActionListener(e -> {
            String paperName = paperNameField.getText().trim();
            String subject = subjectCombo.getSelectedItem() != null
                    ? subjectCombo.getSelectedItem().toString().trim()
                    : "";

            if (paperName.isEmpty()) {
                UIUtil.showWarning(dialog, "试卷名称不能为空");
                return;
            }
            if (subject.isEmpty()) {
                UIUtil.showWarning(dialog, "科目不能为空");
                return;
            }

            try {
                paper.setPaperName(paperName);
                paper.setSubject(subject);
                paper.setDuration((Integer) durationSpinner.getValue());
                paper.setPassScore((Integer) passScoreSpinner.getValue());
                paper.setDescription(descArea.getText().trim());

                paperService.updatePaper(paper);

                UIUtil.showInfo(dialog, "试卷修改成功！");
                dialog.dispose();
                // 通知主框架刷新数据
                mainFrame.refreshPaperData();

            } catch (Exception ex) {
                UIUtil.showError(dialog, "修改试卷失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        });

        JButton cancelButton = TeacherUIHelper.createStyledButton("取消", new Color(120, 144, 156));
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    /**
     * 显示试卷详细信息对话框
     */
    public void showPaperDetailDialog(Paper paper) {
        JDialog dialog = new JDialog(mainFrame, "试卷详情", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 标题
        JLabel titleLabel = new JLabel("试卷详细信息");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 试卷基本信息
        JPanel infoPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("基本信息"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        infoPanel.add(createInfoLabel("试卷名称："));
        infoPanel.add(createInfoValueLabel(paper.getPaperName()));

        infoPanel.add(createInfoLabel("科目："));
        infoPanel.add(createInfoValueLabel(paper.getSubject()));

        infoPanel.add(createInfoLabel("题目数量："));
        int questionCount = paper.getQuestions() != null ? paper.getQuestions().size() : 0;
        infoPanel.add(createInfoValueLabel(questionCount + " 道"));

        infoPanel.add(createInfoLabel("总分："));
        infoPanel.add(createInfoValueLabel(paper.getTotalScore() + " 分"));

        infoPanel.add(createInfoLabel("考试时长："));
        infoPanel.add(createInfoValueLabel(paper.getDuration() + " 分钟"));

        infoPanel.add(createInfoLabel("及格分数："));
        infoPanel.add(createInfoValueLabel(paper.getPassScore() + " 分"));

        // 题目列表
        JPanel questionsPanel = new JPanel(new BorderLayout(0, 10));
        questionsPanel.setBackground(Color.WHITE);
        questionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("题目列表"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        String[] columns = {"序号", "类型", "题目内容", "分值"};
        DefaultTableModel questionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (paper.getQuestions() != null) {
            int index = 1;
            for (Question q : paper.getQuestions()) {
                Object[] row = {
                        index++,
                        q.getQuestionType().getDescription(),
                        TeacherUIHelper.truncate(q.getContent(), 60),
                        q.getScore() + "分"
                };
                questionTableModel.addRow(row);
            }
        }

        JTable questionTable = new JTable(questionTableModel);
        questionTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        questionTable.setRowHeight(35);
        questionTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        questionTable.getTableHeader().setBackground(new Color(245, 247, 250));

        JScrollPane scrollPane = new JScrollPane(questionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        questionsPanel.add(scrollPane, BorderLayout.CENTER);

        // 组合面板
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(infoPanel, BorderLayout.NORTH);
        centerPanel.add(questionsPanel, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        JButton closeButton = TeacherUIHelper.createStyledButton("关闭", UIUtil.PRIMARY_COLOR);
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    /**
     * 删除试卷
     */
    public void deletePaper(Paper paper) {
        if (!UIUtil.showConfirm(mainFrame, "确定要删除这份试卷吗？\n删除后将无法恢复！")) {
            return;
        }

        try {
            // 删除试卷
            paperService.deletePaper(paper.getPaperId());

            UIUtil.showInfo(mainFrame, "删除成功");
            // 通知主框架刷新数据
            mainFrame.refreshPaperData();
        } catch (Exception e) {
            UIUtil.showError(mainFrame, "删除失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 切换试卷发布状态
     */
    public void togglePublishStatus(Paper paper) {
        String action = ""; // 声明在外部，以便在catch块中使用

        try {
            boolean currentStatus = paper.getIsPublished() != null && paper.getIsPublished();
            action = currentStatus ? "取消发布" : "发布";

            int confirm = JOptionPane.showConfirmDialog(
                    mainFrame,
                    "确定要" + action + "试卷《" + paper.getPaperName() + "》吗？",
                    action + "确认",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (currentStatus) {
                    paperService.unpublishPaper(paper.getPaperId());
                    UIUtil.showInfo(mainFrame, "试卷已取消发布");
                } else {
                    paperService.publishPaper(paper.getPaperId());
                    UIUtil.showInfo(mainFrame, "试卷已发布，学生端现在可以看到该试卷了");
                }
                // 通知主框架刷新数据
                mainFrame.refreshPaperData();
            }
        } catch (Exception e) {
            UIUtil.showError(mainFrame, action + "失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        label.setForeground(new Color(100, 100, 100));
        return label;
    }

    private JLabel createInfoValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.BOLD, 13));
        label.setForeground(UIUtil.TEXT_COLOR);
        return label;
    }
}