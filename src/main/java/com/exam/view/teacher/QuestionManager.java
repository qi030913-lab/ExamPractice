package com.exam.view.teacher;

import com.exam.model.Question;
import com.exam.model.enums.Difficulty;
import com.exam.model.enums.QuestionType;
import com.exam.service.QuestionService;
import com.exam.util.QuestionImportUtil;
import com.exam.util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * 题目管理器 - 处理所有与题目相关的操作
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
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(mainFrame);

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
        // 使用下拉框选择科目，去掉"全部"选项
        String[] subjectOptions = TeacherConstants.getSubjectsWithoutAll();
        JComboBox<String> subjectCombo = new JComboBox<>(subjectOptions);
        subjectCombo.setEditable(true); // 允许输入自定义科目
        // 设置默认值
        subjectCombo.setSelectedItem("Java"); // 默认选择Java
        panel.add(subjectCombo, gbc);

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
                // 从下拉框获取科目
                String selectedSubject = subjectCombo.getSelectedItem() != null
                        ? subjectCombo.getSelectedItem().toString().trim()
                        : "";
                question.setSubject(selectedSubject);
                question.setContent(contentArea.getText().trim());
                question.setOptionA(optionAField.getText().trim());
                question.setOptionB(optionBField.getText().trim());
                question.setOptionC(optionCField.getText().trim());
                question.setOptionD(optionDField.getText().trim());
                question.setCorrectAnswer(answerField.getText().trim());
                question.setScore((Integer) scoreSpinner.getValue());
                question.setDifficulty((Difficulty) difficultyCombo.getSelectedItem());
                question.setCreatorId(mainFrame.getTeacher().getUserId());

                questionService.addQuestion(question);
                UIUtil.showInfo(dialog, "添加成功");
                dialog.dispose();
                // 通知主框架刷新数据
                mainFrame.refreshQuestionData();
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
    
    /**
     * 显示编辑题目对话框
     */
    public void showEditQuestionDialog(Question question) {
        JDialog dialog = new JDialog(mainFrame, "编辑题目", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 题目类型
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("题目类型:"), gbc);
        gbc.gridx = 1;
        JComboBox<QuestionType> typeCombo = new JComboBox<>(QuestionType.values());
        typeCombo.setSelectedItem(question.getQuestionType());
        panel.add(typeCombo, gbc);

        // 科目
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("科目:"), gbc);
        gbc.gridx = 1;
        // 使用下拉框选择科目，去掉"全部"选项
        String[] subjectOptions = TeacherConstants.getSubjectsWithoutAll();
        JComboBox<String> subjectCombo = new JComboBox<>(subjectOptions);
        subjectCombo.setEditable(true); // 允许输入自定义科目
        subjectCombo.setSelectedItem(question.getSubject());
        panel.add(subjectCombo, gbc);

        // 题目内容
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("题目内容:"), gbc);
        gbc.gridx = 1;
        JTextArea contentArea = new JTextArea(3, 20);
        contentArea.setLineWrap(true);
        contentArea.setText(question.getContent());
        panel.add(new JScrollPane(contentArea), gbc);

        // 选项A
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("选项A:"), gbc);
        gbc.gridx = 1;
        JTextField optionAField = new JTextField(20);
        optionAField.setText(question.getOptionA());
        panel.add(optionAField, gbc);

        // 选项B
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("选项B:"), gbc);
        gbc.gridx = 1;
        JTextField optionBField = new JTextField(20);
        optionBField.setText(question.getOptionB());
        panel.add(optionBField, gbc);

        // 选项C
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("选项C:"), gbc);
        gbc.gridx = 1;
        JTextField optionCField = new JTextField(20);
        optionCField.setText(question.getOptionC());
        panel.add(optionCField, gbc);

        // 选项D
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("选项D:"), gbc);
        gbc.gridx = 1;
        JTextField optionDField = new JTextField(20);
        optionDField.setText(question.getOptionD());
        panel.add(optionDField, gbc);

        // 正确答案
        gbc.gridx = 0; gbc.gridy = 7;
        panel.add(new JLabel("正确答案:"), gbc);
        gbc.gridx = 1;
        JTextField answerField = new JTextField(20);
        answerField.setText(question.getCorrectAnswer());
        panel.add(answerField, gbc);

        // 分值
        gbc.gridx = 0; gbc.gridy = 8;
        panel.add(new JLabel("分值:"), gbc);
        gbc.gridx = 1;
        JSpinner scoreSpinner = new JSpinner(new SpinnerNumberModel(question.getScore().intValue(), 1, 100, 1));
        panel.add(scoreSpinner, gbc);

        // 难度
        gbc.gridx = 0; gbc.gridy = 9;
        panel.add(new JLabel("难度:"), gbc);
        gbc.gridx = 1;
        JComboBox<Difficulty> difficultyCombo = new JComboBox<>(Difficulty.values());
        difficultyCombo.setSelectedItem(question.getDifficulty());
        panel.add(difficultyCombo, gbc);

        // 按钮
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        JButton saveButton = UIUtil.createSuccessButton("保存");
        JButton cancelButton = UIUtil.createDangerButton("取消");

        saveButton.addActionListener(e -> {
            try {
                question.setQuestionType((QuestionType) typeCombo.getSelectedItem());
                // 从下拉框获取科目
                String selectedSubject = subjectCombo.getSelectedItem() != null
                        ? subjectCombo.getSelectedItem().toString().trim()
                        : "";
                question.setSubject(selectedSubject);
                question.setContent(contentArea.getText().trim());
                question.setOptionA(optionAField.getText().trim());
                question.setOptionB(optionBField.getText().trim());
                question.setOptionC(optionCField.getText().trim());
                question.setOptionD(optionDField.getText().trim());
                question.setCorrectAnswer(answerField.getText().trim());
                question.setScore((Integer) scoreSpinner.getValue());
                question.setDifficulty((Difficulty) difficultyCombo.getSelectedItem());

                questionService.updateQuestion(question);
                UIUtil.showInfo(dialog, "修改成功");
                dialog.dispose();
                // 通知主框架刷新数据
                mainFrame.refreshQuestionData();
            } catch (Exception ex) {
                UIUtil.showError(dialog, "修改失败：" + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        btnPanel.add(saveButton);
        btnPanel.add(cancelButton);
        panel.add(btnPanel, gbc);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }
    
    /**
     * 删除题目
     */
    public void deleteQuestion(Question question) {
        if (!UIUtil.showConfirm(mainFrame, "确定要删除这道题目吗？")) {
            return;
        }

        try {
            questionService.deleteQuestion(question.getQuestionId());
            UIUtil.showInfo(mainFrame, "删除成功");
            // 通知主框架刷新数据
            mainFrame.refreshQuestionData();
        } catch (Exception e) {
            UIUtil.showError(mainFrame, "删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 从文件导入题目
     */
    public void importQuestionsFromFile(File file) {
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
            // 通知主框架刷新数据
            mainFrame.refreshQuestionData();
        } catch (Exception e) {
            UIUtil.showError(mainFrame, "导入题目失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 导入题目并生成试卷
     */
    private void importAndGeneratePaper(List<Question> questions) {
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
                // 通知主框架刷新数据
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