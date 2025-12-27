package com.exam.view.teacher;

import com.exam.model.Question;
import com.exam.service.QuestionService;
import com.exam.util.QuestionImportUtil;
import com.exam.util.UIUtil;
import com.exam.view.LoginFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * 导入功能管理器
 */
public class ImportManager {
    private final QuestionService questionService;
    private final TeacherMainFrame mainFrame;
    
    // 存储选择的文件
    private File selectedImportFile = null;

    public ImportManager(QuestionService questionService, TeacherMainFrame mainFrame) {
        this.questionService = questionService;
        this.mainFrame = mainFrame;
    }

    /**
     * 创建文件选择面板
     */
    public JPanel createFileSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 220, 240), 2),
                        "选择导入文件",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("微软雅黑", Font.BOLD, 14),
                        UIUtil.PRIMARY_COLOR
                ),
                BorderFactory.createEmptyBorder(10, 20, 15, 20)
        ));

        // 所有内容放在一行：文件图标 + 文件信息 + 按钮
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        contentPanel.setBackground(Color.WHITE);

        // 文件图标
        JLabel fileIconLabel = new JLabel("📄");
        fileIconLabel.setFont(new Font("微软雅黑", Font.PLAIN, 32));
        contentPanel.add(fileIconLabel);

        // 文件信息面板（包含关闭按钮）
        JPanel fileInfoWrapper = new JPanel(new BorderLayout(5, 0));
        fileInfoWrapper.setBackground(Color.WHITE);

        JPanel fileDetailsPanel = new JPanel();
        fileDetailsPanel.setLayout(new BoxLayout(fileDetailsPanel, BoxLayout.Y_AXIS));
        fileDetailsPanel.setBackground(Color.WHITE);

        JLabel fileNameLabel = new JLabel("未选择文件");
        fileNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        fileNameLabel.setForeground(new Color(100, 100, 100));
        fileNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel filePathLabel = new JLabel("请点击右侧按钮选择题目文件（.txt格式）");
        filePathLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        filePathLabel.setForeground(new Color(120, 120, 120));
        filePathLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        fileDetailsPanel.add(fileNameLabel);
        fileDetailsPanel.add(Box.createVerticalStrut(3));
        fileDetailsPanel.add(filePathLabel);

        fileInfoWrapper.add(fileDetailsPanel, BorderLayout.CENTER);

        // 关闭按钮（右上角的×）
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
        closeButton.setForeground(new Color(150, 150, 150));
        closeButton.setBackground(Color.WHITE);
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setVisible(false); // 初始隐藏
        closeButton.setPreferredSize(new Dimension(25, 25));

        // 鼠标悬停效果
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(UIUtil.DANGER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(new Color(150, 150, 150));
            }
        });

        // 清除文件选择
        closeButton.addActionListener(e -> {
            selectedImportFile = null;
            fileNameLabel.setText("未选择文件");
            fileNameLabel.setForeground(new Color(100, 100, 100));
            filePathLabel.setText("请点击右侧按钮选择题目文件（.txt格式）");
            closeButton.setVisible(false);
        });

        fileInfoWrapper.add(closeButton, BorderLayout.EAST);
        contentPanel.add(fileInfoWrapper);

        // 添加一些水平间隙
        contentPanel.add(Box.createHorizontalStrut(20));

        // 选择文件按钮
        JButton selectFileButton = TeacherUIHelper.createStyledButton("选择文件", UIUtil.PRIMARY_COLOR);
        selectFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择题目文件");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("文本文件 (*.txt)", "txt");
            fileChooser.setFileFilter(filter);

            int result = fileChooser.showOpenDialog(mainFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImportFile = fileChooser.getSelectedFile();
                fileNameLabel.setText(selectedImportFile.getName());
                fileNameLabel.setForeground(UIUtil.PRIMARY_COLOR);
                filePathLabel.setText(selectedImportFile.getAbsolutePath());
                closeButton.setVisible(true); // 显示关闭按钮
            }
        });
        contentPanel.add(selectFileButton);

        // 开始导入按钮
        JButton importButton = TeacherUIHelper.createStyledButton("开始导入", UIUtil.SUCCESS_COLOR);
        importButton.addActionListener(e -> startImport());
        contentPanel.add(importButton);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建格式说明面板
     */
    public JPanel createFormatDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 220, 240), 2),
                        "文件格式说明",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("微软雅黑", Font.BOLD, 14),
                        UIUtil.PRIMARY_COLOR
                ),
                BorderFactory.createEmptyBorder(15, 20, 20, 20)
        ));

        // 格式说明文本
        JTextArea formatText = new JTextArea();
        formatText.setEditable(false);
        formatText.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        formatText.setForeground(new Color(80, 80, 80));
        formatText.setBackground(new Color(248, 250, 252));
        formatText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formatText.setLineWrap(true);
        formatText.setWrapStyleWord(true);

        String formatInfo = "文件格式：题目类型|科目|题目内容|选项A|选项B|选项C|选项D|正确答案|分值|难度|解析\n\n"
                + "• 题目类型：SINGLE(单选)、MULTIPLE(多选)、JUDGE(判断)、BLANK(填空)\n"
                + "• 难度：EASY(简单)、MEDIUM(中等)、HARD(困难)\n"
                + "• 以#开头的行为注释，会被忽略\n\n"
                + "示例：\n"
                + "SINGLE|Java|Java中哪个关键字用于定义常量？|const|final|static|constant|B|5|EASY|解析内容";

        formatText.setText(formatInfo);

        JScrollPane scrollPane = new JScrollPane(formatText);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.setPreferredSize(new Dimension(0, 120));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建导入操作面板
     */
    public JPanel createImportActionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 220, 240), 2),
                        "导入操作",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("微软雅黑", Font.BOLD, 14),
                        UIUtil.PRIMARY_COLOR
                ),
                BorderFactory.createEmptyBorder(10, 20, 15, 20)
        ));

        // 主内容面板：使用BorderLayout将提示和按钮放在同一行
        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setBackground(Color.WHITE);

        // 左侧：提示信息
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        infoPanel.setBackground(new Color(255, 248, 225));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 193, 7)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel infoIcon = new JLabel("ℹ️");
        infoIcon.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JLabel infoLabel = new JLabel("导入前请确保文件格式正确，可先下载模板参考");
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(102, 77, 3));

        infoPanel.add(infoIcon);
        infoPanel.add(infoLabel);

        contentPanel.add(infoPanel, BorderLayout.CENTER);

        // 右侧：按钮区域（只保留下载模板按钮）
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton downloadTemplateButton = TeacherUIHelper.createStyledButton("下载模板文件", new Color(52, 152, 219));
        downloadTemplateButton.addActionListener(e -> downloadTemplate());

        buttonPanel.add(downloadTemplateButton);

        contentPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 下载模板文件
     */
    private void downloadTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存模板文件");
        fileChooser.setSelectedFile(new File("题目导入模板.txt"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("文本文件 (*.txt)", "txt");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showSaveDialog(mainFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // 确保文件扩展名为.txt
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }

            try {
                QuestionImportUtil.generateTemplate(file);
                UIUtil.showInfo(mainFrame, "模板文件已保存到：\n" + file.getAbsolutePath());
            } catch (Exception e) {
                UIUtil.showError(mainFrame, "保存模板文件失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 开始导入
     */
    private void startImport() {
        if (selectedImportFile == null) {
            UIUtil.showWarning(mainFrame, "请先选择要导入的文件");
            return;
        }

        if (!selectedImportFile.exists()) {
            UIUtil.showError(mainFrame, "文件不存在，请重新选择");
            selectedImportFile = null;
            return;
        }

        importQuestionsFromFile(selectedImportFile);
    }

    /**
     * 从文件导入题目
     */
    private void importQuestionsFromFile(File file) {
        try {
            // 读取题目
            List<Question> questions = QuestionImportUtil.importFromTextFile(file, mainFrame.getTeacher().getUserId());

            if (questions.isEmpty()) {
                UIUtil.showWarning(mainFrame, "文件中没有有效的题目数据");
                return;
            }

            // 显示确认对话框
            String message = "成功读取 " + questions.size() + " 道题目\n\n"
                    + "请选择操作：\n"
                    + "1. 仅导入题目到题库\n"
                    + "2. 导入并自动生成试卷";

            Object[] options = {"仅导入题目", "导入并生成试卷", "取消"};
            int choice = JOptionPane.showOptionDialog(mainFrame,
                    message,
                    "题目导入",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);

            if (choice == 0) {
                // 仅导入题目
                importQuestionsOnly(questions);
            } else if (choice == 1) {
                // 导入并生成试卷
                importAndGeneratePaper(questions);
            }

        } catch (Exception e) {
            UIUtil.showError(mainFrame, "导入失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 仅导入题目到题库
     */
    private void importQuestionsOnly(List<Question> questions) {
        try {
            questionService.batchAddQuestions(questions);
            UIUtil.showInfo(mainFrame, "成功导入 " + questions.size() + " 道题目！");
            mainFrame.refreshQuestionData();
        } catch (Exception e) {
            UIUtil.showError(mainFrame, "导入题目失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 导入题目并生成试卷
     */
    public void importAndGeneratePaper(List<Question> questions) {
        // 显示试卷信息输入对话框
        JDialog dialog = new JDialog(mainFrame, "生成试卷", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(new Color(245, 250, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 标题
        JLabel titleLabel = new JLabel("设置试卷信息");
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
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(8, 8, 8, 8);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

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
        // 使用下拉框选择科目
        String[] subjectOptions = TeacherConstants.getSubjectsWithoutAll();
        JComboBox<String> subjectCombo = new JComboBox<>(subjectOptions);
        subjectCombo.setEditable(true);
        subjectCombo.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        // 自动填充科目（从题目中获取）
        if (!questions.isEmpty()) {
            subjectCombo.setSelectedItem(questions.get(0).getSubject());
        }
        formPanel.add(subjectCombo, gbc);

        // 考试时长
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel durationLabel = new JLabel("时长(分钟)：");
        durationLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        formPanel.add(durationLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner durationSpinner = new JSpinner(new javax.swing.SpinnerNumberModel(90, 10, 300, 10));
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
        JSpinner passScoreSpinner = new JSpinner(new javax.swing.SpinnerNumberModel(60, 0, 100, 5));
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

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(245, 250, 255));

        JButton confirmButton = TeacherUIHelper.createStyledButton("生成试卷", UIUtil.PRIMARY_COLOR);
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

            try {
                // 先导入题目
                List<Integer> questionIds = questionService.batchAddQuestions(questions);

                // 创建试卷
                com.exam.model.Paper paper = new com.exam.model.Paper();
                paper.setPaperName(paperName);
                paper.setSubject(subject);
                paper.setDuration((Integer) durationSpinner.getValue());
                paper.setPassScore((Integer) passScoreSpinner.getValue());
                paper.setDescription(descArea.getText().trim());
                paper.setCreatorId(mainFrame.getTeacher().getUserId());

                int paperId = mainFrame.getPaperService().createPaper(paper, questionIds);

                UIUtil.showInfo(dialog, "成功生成试卷！\n导入题目：" + questions.size() + " 道");
                dialog.dispose();
                mainFrame.refreshQuestionData();
                mainFrame.refreshPaperData();

            } catch (Exception ex) {
                UIUtil.showError(dialog, "生成试卷失败：" + ex.getMessage());
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
}