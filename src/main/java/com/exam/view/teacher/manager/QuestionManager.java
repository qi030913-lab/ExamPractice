package com.exam.view.teacher.manager;

import com.exam.model.Question;
import com.exam.model.Paper;
import com.exam.model.enums.QuestionType;
import com.exam.model.enums.Difficulty;
import com.exam.service.QuestionService;
import com.exam.util.UIUtil;
import com.exam.view.teacher.TeacherMainFrame;
import com.exam.view.teacher.TeacherUIHelper;

import com.exam.view.teacher.TeacherConstants;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题库管理器 - 处理所有与题库相关的操作
 */
public class QuestionManager {
    private final QuestionService questionService;
    private final TeacherMainFrame mainFrame;

    public QuestionManager(QuestionService questionService, TeacherMainFrame mainFrame) {
        this.questionService = questionService;
        this.mainFrame = mainFrame;
    }

    /**
     * 显示添加题目对话框
     */
    public void showAddQuestionDialog() {
        JDialog dialog = new JDialog(mainFrame, "添加题目", true);
        dialog.setSize(800, 700);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(new Color(245, 250, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 标题
        JLabel titleLabel = new JLabel("添加新题目");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("题目信息"));

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 题目类型
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("题目类型："), gbc);
        JComboBox<QuestionType> typeBox = new JComboBox<>(QuestionType.values());
        gbc.gridx = 1;
        formPanel.add(typeBox, gbc);

        // 科目
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("科目："), gbc);
        JComboBox<String> subjectBox = new JComboBox<>(TeacherConstants.getSubjectsWithoutAll());
        subjectBox.setEditable(true);
        gbc.gridx = 1;
        formPanel.add(subjectBox, gbc);

        // 难度
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("难度："), gbc);
        JComboBox<Difficulty> difficultyBox = new JComboBox<>(Difficulty.values());
        gbc.gridx = 1;
        formPanel.add(difficultyBox, gbc);

        // 题目内容
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("题目内容："), gbc);
        JTextArea contentArea = new JTextArea(2, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);
        gbc.gridx = 1;
        formPanel.add(contentScroll, gbc);

        // 选项A
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("选项A："), gbc);
        JTextField optionAField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(optionAField, gbc);

        // 选项B
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("选项B："), gbc);
        JTextField optionBField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(optionBField, gbc);

        // 选项C
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("选项C："), gbc);
        JTextField optionCField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(optionCField, gbc);

        // 选项D
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("选项D："), gbc);
        JTextField optionDField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(optionDField, gbc);

        // 正确答案
        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(new JLabel("正确答案："), gbc);
        JTextField answerField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(answerField, gbc);

        // 分值
        gbc.gridx = 0; gbc.gridy = 9;
        formPanel.add(new JLabel("分值："), gbc);
        JSpinner scoreSpinner = new JSpinner(new javax.swing.SpinnerNumberModel((int)5, (int)1, (int)100, (int)1));
        gbc.gridx = 1;
        formPanel.add(scoreSpinner, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));

        JButton confirmButton = new JButton("添加题目");
        confirmButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        confirmButton.setBackground(UIUtil.SUCCESS_COLOR);
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        confirmButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        confirmButton.addActionListener(e -> {
            QuestionType type = (QuestionType) typeBox.getSelectedItem();
            String subject = (String) subjectBox.getSelectedItem();
            Difficulty difficulty = (Difficulty) difficultyBox.getSelectedItem();
            String content = contentArea.getText().trim();
            String optionA = optionAField.getText().trim();
            String optionB = optionBField.getText().trim();
            String optionC = optionCField.getText().trim();
            String optionD = optionDField.getText().trim();
            String answer = answerField.getText().trim();
            int score = (Integer) scoreSpinner.getValue();

            if (content.isEmpty()) {
                UIUtil.showWarning(dialog, "题目内容不能为空");
                return;
            }
            if (subject == null || subject.trim().isEmpty()) {
                UIUtil.showWarning(dialog, "科目不能为空");
                return;
            }
            if (answer.isEmpty()) {
                UIUtil.showWarning(dialog, "正确答案不能为空");
                return;
            }

            try {
                Question question = new Question();
                question.setQuestionType(type);
                question.setSubject(subject.trim());
                question.setDifficulty(difficulty);
                question.setContent(content);
                question.setOptionA(optionA.isEmpty() ? null : optionA);
                question.setOptionB(optionB.isEmpty() ? null : optionB);
                question.setOptionC(optionC.isEmpty() ? null : optionC);
                question.setOptionD(optionD.isEmpty() ? null : optionD);
                question.setCorrectAnswer(answer);
                question.setScore(score);

                questionService.addQuestion(question);

                UIUtil.showInfo(dialog, "题目添加成功！");
                dialog.dispose();
                // 通知主框架刷新数据
                mainFrame.refreshQuestionData();

            } catch (Exception ex) {
                UIUtil.showError(dialog, "添加题目失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        });
        buttonPanel.add(confirmButton);

        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cancelButton.setBackground(new Color(120, 144, 156));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * 显示编辑题目对话框
     */
    public void showEditQuestionDialog(Question question) {
        JDialog dialog = new JDialog(mainFrame, "编辑题目", true);
        dialog.setSize(800, 700);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(new Color(245, 250, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 标题
        JLabel titleLabel = new JLabel("编辑题目");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("题目信息"));

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 题目类型
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("题目类型："), gbc);
        JComboBox<QuestionType> typeBox = new JComboBox<>(QuestionType.values());
        typeBox.setSelectedItem(question.getQuestionType());
        gbc.gridx = 1;
        formPanel.add(typeBox, gbc);

        // 科目
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("科目："), gbc);
        JComboBox<String> subjectBox = new JComboBox<>(TeacherConstants.getSubjectsWithoutAll());
        subjectBox.setEditable(true);
        subjectBox.setSelectedItem(question.getSubject());
        gbc.gridx = 1;
        formPanel.add(subjectBox, gbc);

        // 难度
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("难度："), gbc);
        JComboBox<Difficulty> difficultyBox = new JComboBox<>(Difficulty.values());
        difficultyBox.setSelectedItem(question.getDifficulty());
        gbc.gridx = 1;
        formPanel.add(difficultyBox, gbc);

        // 题目内容
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("题目内容："), gbc);
        JTextArea contentArea = new JTextArea(question.getContent(), 2, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);
        gbc.gridx = 1;
        formPanel.add(contentScroll, gbc);

        // 选项A
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("选项A："), gbc);
        JTextField optionAField = new JTextField(question.getOptionA());
        gbc.gridx = 1;
        formPanel.add(optionAField, gbc);

        // 选项B
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("选项B："), gbc);
        JTextField optionBField = new JTextField(question.getOptionB());
        gbc.gridx = 1;
        formPanel.add(optionBField, gbc);

        // 选项C
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("选项C："), gbc);
        JTextField optionCField = new JTextField(question.getOptionC());
        gbc.gridx = 1;
        formPanel.add(optionCField, gbc);

        // 选项D
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("选项D："), gbc);
        JTextField optionDField = new JTextField(question.getOptionD());
        gbc.gridx = 1;
        formPanel.add(optionDField, gbc);

        // 正确答案
        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(new JLabel("正确答案："), gbc);
        JTextField answerField = new JTextField(question.getCorrectAnswer());
        gbc.gridx = 1;
        formPanel.add(answerField, gbc);

        // 分值
        gbc.gridx = 0; gbc.gridy = 9;
        formPanel.add(new JLabel("分值："), gbc);
        JSpinner scoreSpinner = new JSpinner(new javax.swing.SpinnerNumberModel((int)question.getScore().intValue(), (int)1, (int)100, (int)1));
        gbc.gridx = 1;
        formPanel.add(scoreSpinner, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));

        JButton saveButton = new JButton("保存修改");
        saveButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        saveButton.setBackground(UIUtil.SUCCESS_COLOR);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        saveButton.addActionListener(e -> {
            QuestionType type = (QuestionType) typeBox.getSelectedItem();
            String subject = (String) subjectBox.getSelectedItem();
            Difficulty difficulty = (Difficulty) difficultyBox.getSelectedItem();
            String content = contentArea.getText().trim();
            String optionA = optionAField.getText().trim();
            String optionB = optionBField.getText().trim();
            String optionC = optionCField.getText().trim();
            String optionD = optionDField.getText().trim();
            String answer = answerField.getText().trim();
            int score = (Integer) scoreSpinner.getValue();

            if (content.isEmpty()) {
                UIUtil.showWarning(dialog, "题目内容不能为空");
                return;
            }
            if (subject == null || subject.trim().isEmpty()) {
                UIUtil.showWarning(dialog, "科目不能为空");
                return;
            }
            if (answer.isEmpty()) {
                UIUtil.showWarning(dialog, "正确答案不能为空");
                return;
            }

            try {
                question.setQuestionType(type);
                question.setSubject(subject.trim());
                question.setDifficulty(difficulty);
                question.setContent(content);
                question.setOptionA(optionA.isEmpty() ? null : optionA);
                question.setOptionB(optionB.isEmpty() ? null : optionB);
                question.setOptionC(optionC.isEmpty() ? null : optionC);
                question.setOptionD(optionD.isEmpty() ? null : optionD);
                question.setCorrectAnswer(answer);
                question.setScore(score);

                questionService.updateQuestion(question);

                UIUtil.showInfo(dialog, "题目修改成功！");
                dialog.dispose();
                // 通知主框架刷新数据
                mainFrame.refreshQuestionData();

            } catch (Exception ex) {
                UIUtil.showError(dialog, "修改题目失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        });
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cancelButton.setBackground(new Color(120, 144, 156));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * 删除题目
     */
    public void deleteQuestion(Question question) {
        if (!UIUtil.showConfirm(mainFrame, "确定要删除这道题目吗？\n删除后将无法恢复！")) {
            return;
        }

        try {
            // 检查题目是否被试卷使用（性能优化版本）
            List<Paper> usedPapers = mainFrame.getPaperService().getPaperDao().findPapersUsingQuestion(question.getQuestionId());

            if (!usedPapers.isEmpty()) {
                StringBuilder msg = new StringBuilder("该题目已被以下试卷使用，删除将影响这些试卷：\n\n");
                for (Paper p : usedPapers) {
                    msg.append("- ").append(p.getPaperName()).append("\n");
                }
                msg.append("\n确定要继续删除吗？");

                if (!UIUtil.showConfirm(mainFrame, msg.toString())) {
                    return;
                }
            }

            // 删除题目
            questionService.deleteQuestion(question.getQuestionId());

            UIUtil.showInfo(mainFrame, "删除成功");
            // 通知主框架刷新数据
            mainFrame.refreshQuestionData();
        } catch (Exception e) {
            UIUtil.showError(mainFrame, "删除失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 导入题目并自动生成试卷
     */
    public void importAndGeneratePaper(List<Question> questions) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainFrame), "自动生成试卷", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 表单面板
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createTitledBorder("试卷信息"));

        JLabel nameLabel = new JLabel("试卷名称：");
        JTextField nameField = new JTextField("导入题目_" + System.currentTimeMillis());
        formPanel.add(nameLabel);
        formPanel.add(nameField);

        JLabel subjectLabel = new JLabel("科目：");
        String[] subjects = {"Java", "Vue", "数据结构", "马克思主义", "计算机网络", "操作系统", "数据库", "英语", "其他"};
        JComboBox<String> subjectBox = new JComboBox<>(subjects);
        formPanel.add(subjectLabel);
        formPanel.add(subjectBox);

        JLabel passScoreLabel = new JLabel("及格分数：");
        JSpinner passScoreSpinner = new JSpinner(new javax.swing.SpinnerNumberModel(60, 0, 100, 1));
        formPanel.add(passScoreLabel);
        formPanel.add(passScoreSpinner);

        JLabel durationLabel = new JLabel("考试时长(分钟)：");
        JSpinner durationSpinner = new JSpinner(new javax.swing.SpinnerNumberModel(90, 1, 300, 5));
        formPanel.add(durationLabel);
        formPanel.add(durationSpinner);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        JButton confirmButton = new JButton("确定");
        confirmButton.addActionListener(e -> {
            String paperName = nameField.getText().trim();
            String subject = (String) subjectBox.getSelectedItem();
            int passScore = (Integer) passScoreSpinner.getValue();
            int duration = (Integer) durationSpinner.getValue();

            if (paperName.isEmpty()) {
                UIUtil.showWarning(dialog, "请输入试卷名称");
                return;
            }

            try {
                // 创建试卷对象
                Paper paper = new Paper();
                paper.setPaperName(paperName);
                paper.setSubject(subject);
                paper.setPassScore(passScore);
                paper.setDuration(duration);
                paper.setCreatorId(mainFrame.getTeacher().getUserId());

                // 计算总分
                int totalScore = questions.stream()
                        .mapToInt(Question::getScore)
                        .sum();
                paper.setTotalScore(totalScore);

                // 创建试卷并关联题目
                int paperId = mainFrame.getPaperService().createPaper(paper, questions.stream().mapToInt(Question::getQuestionId).boxed().collect(java.util.stream.Collectors.toList()));

                UIUtil.showInfo(dialog, "成功生成试卷！\n导入题目：" + questions.size() + " 道");
                dialog.dispose();
                mainFrame.refreshQuestionData();
                mainFrame.refreshPaperData();

            } catch (Exception ex) {
                UIUtil.showError(dialog, "生成试卷失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        });

        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
}