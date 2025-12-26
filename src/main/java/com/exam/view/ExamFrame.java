package com.exam.view;

import com.exam.model.*;
import com.exam.model.enums.QuestionType;
import com.exam.service.ExamService;
import com.exam.util.UIUtil;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考试界面
 * 使用多线程实现倒计时功能
 */
public class ExamFrame extends JFrame {
    private final User student;
    private final Paper paper;
    private final ExamService examService;
    private final ExamRecord examRecord;
    
    private List<Question> questions;
    private Map<Integer, String> answers;
    private int currentQuestionIndex = 0;
    
    private JLabel timerLabel;
    private JLabel questionLabel;
    private JTextArea contentArea;
    private ButtonGroup optionGroup;
    private JRadioButton optionA, optionB, optionC, optionD;
    private JCheckBox checkA, checkB, checkC, checkD;
    private JPanel optionPanel;
    
    private Timer countdownTimer;
    private int remainingSeconds;
    private boolean submitted = false;

    public ExamFrame(User student, Paper paper, ExamService examService) {
        this.student = student;
        this.paper = paper;
        this.examService = examService;
        this.questions = paper.getQuestions();
        this.answers = new HashMap<>();
        
        // 开始考试
        this.examRecord = examService.startExam(student.getUserId(), paper.getPaperId());
        this.remainingSeconds = paper.getDuration() * 60;
        
        initComponents();
        setTitle("在线考试 - " + paper.getPaperName());
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleExit();
            }
        });
        UIUtil.centerWindow(this);
        
        // 显示第一题
        showQuestion(0);
        
        // 启动倒计时
        startCountdown();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // 顶部信息面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIUtil.PRIMARY_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel(paper.getPaperName() + " - " + student.getRealName());
        titleLabel.setFont(UIUtil.HEADING_FONT);
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        timerLabel = new JLabel("剩余时间: " + formatTime(remainingSeconds));
        timerLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        timerLabel.setForeground(Color.YELLOW);
        topPanel.add(timerLabel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 中间答题面板
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 题目信息
        questionLabel = new JLabel();
        questionLabel.setFont(UIUtil.HEADING_FONT);
        centerPanel.add(questionLabel, BorderLayout.NORTH);
        
        // 题目内容
        contentArea = new JTextArea();
        contentArea.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(Color.WHITE);
        contentArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setPreferredSize(new Dimension(800, 120));
        centerPanel.add(contentScroll, BorderLayout.CENTER);
        
        // 选项面板
        optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        centerPanel.add(optionPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // 底部按钮面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton prevButton = UIUtil.createPrimaryButton("上一题");
        prevButton.setPreferredSize(new Dimension(120, 40));
        prevButton.addActionListener(e -> showPrevQuestion());
        
        JButton nextButton = UIUtil.createPrimaryButton("下一题");
        nextButton.setPreferredSize(new Dimension(120, 40));
        nextButton.addActionListener(e -> showNextQuestion());
        
        JButton submitButton = UIUtil.createSuccessButton("提交试卷");
        submitButton.setPreferredSize(new Dimension(120, 40));
        submitButton.addActionListener(e -> submitExam());
        
        bottomPanel.add(prevButton);
        bottomPanel.add(nextButton);
        bottomPanel.add(submitButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) {
            return;
        }
        
        currentQuestionIndex = index;
        Question question = questions.get(index);
        
        // 更新题目信息
        questionLabel.setText(String.format("第 %d/%d 题  [%s] (分值: %d)", 
            index + 1, questions.size(), 
            question.getQuestionType().getDescription(),
            question.getScore()));
        
        contentArea.setText(question.getContent());
        
        // 清空选项面板
        optionPanel.removeAll();
        
        // 根据题目类型显示选项
        if (question.getQuestionType() == QuestionType.SINGLE || 
            question.getQuestionType() == QuestionType.JUDGE) {
            createRadioOptions(question);
        } else if (question.getQuestionType() == QuestionType.MULTIPLE) {
            createCheckboxOptions(question);
        }
        
        optionPanel.revalidate();
        optionPanel.repaint();
    }

    private void createRadioOptions(Question question) {
        optionGroup = new ButtonGroup();
        
        optionA = createRadioButton("A. " + question.getOptionA(), "A");
        optionB = createRadioButton("B. " + question.getOptionB(), "B");
        
        optionPanel.add(optionA);
        optionPanel.add(Box.createVerticalStrut(10));
        optionPanel.add(optionB);
        
        if (question.getQuestionType() == QuestionType.SINGLE) {
            if (question.getOptionC() != null && !question.getOptionC().isEmpty()) {
                optionC = createRadioButton("C. " + question.getOptionC(), "C");
                optionPanel.add(Box.createVerticalStrut(10));
                optionPanel.add(optionC);
            }
            if (question.getOptionD() != null && !question.getOptionD().isEmpty()) {
                optionD = createRadioButton("D. " + question.getOptionD(), "D");
                optionPanel.add(Box.createVerticalStrut(10));
                optionPanel.add(optionD);
            }
        }
        
        // 恢复之前的答案
        String savedAnswer = answers.get(question.getQuestionId());
        if (savedAnswer != null) {
            switch (savedAnswer) {
                case "A": optionA.setSelected(true); break;
                case "B": optionB.setSelected(true); break;
                case "C": if (optionC != null) optionC.setSelected(true); break;
                case "D": if (optionD != null) optionD.setSelected(true); break;
            }
        }
    }

    private JRadioButton createRadioButton(String text, String value) {
        JRadioButton radio = new JRadioButton(text);
        radio.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        radio.setActionCommand(value);
        radio.addActionListener(e -> saveCurrentAnswer());
        optionGroup.add(radio);
        return radio;
    }

    private void createCheckboxOptions(Question question) {
        checkA = createCheckBox("A. " + question.getOptionA(), "A");
        checkB = createCheckBox("B. " + question.getOptionB(), "B");
        checkC = createCheckBox("C. " + question.getOptionC(), "C");
        checkD = createCheckBox("D. " + question.getOptionD(), "D");
        
        optionPanel.add(checkA);
        optionPanel.add(Box.createVerticalStrut(10));
        optionPanel.add(checkB);
        optionPanel.add(Box.createVerticalStrut(10));
        optionPanel.add(checkC);
        optionPanel.add(Box.createVerticalStrut(10));
        optionPanel.add(checkD);
        
        // 恢复之前的答案
        String savedAnswer = answers.get(questions.get(currentQuestionIndex).getQuestionId());
        if (savedAnswer != null) {
            checkA.setSelected(savedAnswer.contains("A"));
            checkB.setSelected(savedAnswer.contains("B"));
            checkC.setSelected(savedAnswer.contains("C"));
            checkD.setSelected(savedAnswer.contains("D"));
        }
    }

    private JCheckBox createCheckBox(String text, String value) {
        JCheckBox check = new JCheckBox(text);
        check.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        check.setActionCommand(value);
        check.addActionListener(e -> saveCurrentAnswer());
        return check;
    }

    private void saveCurrentAnswer() {
        Question question = questions.get(currentQuestionIndex);
        String answer = "";
        
        if (question.getQuestionType() == QuestionType.SINGLE || 
            question.getQuestionType() == QuestionType.JUDGE) {
            if (optionGroup.getSelection() != null) {
                answer = optionGroup.getSelection().getActionCommand();
            }
        } else if (question.getQuestionType() == QuestionType.MULTIPLE) {
            StringBuilder sb = new StringBuilder();
            if (checkA.isSelected()) sb.append("A");
            if (checkB.isSelected()) sb.append("B");
            if (checkC.isSelected()) sb.append("C");
            if (checkD.isSelected()) sb.append("D");
            answer = sb.toString();
        }
        
        if (!answer.isEmpty()) {
            answers.put(question.getQuestionId(), answer);
        }
    }

    private void showPrevQuestion() {
        saveCurrentAnswer();
        if (currentQuestionIndex > 0) {
            showQuestion(currentQuestionIndex - 1);
        }
    }

    private void showNextQuestion() {
        saveCurrentAnswer();
        if (currentQuestionIndex < questions.size() - 1) {
            showQuestion(currentQuestionIndex + 1);
        }
    }

    private void startCountdown() {
        countdownTimer = new Timer(1000, e -> {
            remainingSeconds--;
            timerLabel.setText("剩余时间: " + formatTime(remainingSeconds));
            
            // 时间即将用完时变红
            if (remainingSeconds <= 300) {
                timerLabel.setForeground(Color.RED);
            }
            
            // 时间到，自动交卷
            if (remainingSeconds <= 0) {
                countdownTimer.stop();
                autoSubmit();
            }
        });
        countdownTimer.start();
    }

    private void submitExam() {
        if (submitted) {
            return;
        }
        
        saveCurrentAnswer();
        
        int answered = answers.size();
        int total = questions.size();
        
        String message = String.format("您已答题 %d/%d 题\n确定要提交试卷吗？", answered, total);
        if (!UIUtil.showConfirm(this, message)) {
            return;
        }
        
        doSubmit();
    }

    private void autoSubmit() {
        if (submitted) {
            return;
        }
        
        saveCurrentAnswer();
        UIUtil.showWarning(this, "考试时间已到，系统将自动提交试卷");
        doSubmit();
    }

    private void doSubmit() {
        submitted = true;
        countdownTimer.stop();
        
        try {
            BigDecimal score = examService.submitExam(examRecord.getRecordId(), answers);
            
            String result = String.format(
                "考试完成！\n\n" +
                "试卷：%s\n" +
                "总分：%d 分\n" +
                "得分：%.1f 分\n" +
                "及格分：%d 分\n\n" +
                "%s",
                paper.getPaperName(),
                paper.getTotalScore(),
                score.doubleValue(),
                paper.getPassScore(),
                score.doubleValue() >= paper.getPassScore() ? "恭喜通过！" : "未达到及格线"
            );
            
            UIUtil.showInfo(this, result);
            dispose();
            
        } catch (Exception e) {
            UIUtil.showError(this, "提交失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleExit() {
        if (submitted) {
            dispose();
            return;
        }
        
        if (UIUtil.showConfirm(this, "考试尚未提交，确定要退出吗？\n退出后答题将不会保存。")) {
            countdownTimer.stop();
            dispose();
        }
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}
