package com.exam.view.teacher.ui.components;

import com.exam.model.User;
import com.exam.service.QuestionService;
import com.exam.service.PaperService;
import com.exam.util.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 教师端 - 首页面板
 */
public class TeacherHomePanel extends JPanel {
    private final QuestionService questionService;
    private final PaperService paperService;
    private final User teacher;
    private final com.exam.view.teacher.TeacherMainFrame mainFrame;

    public TeacherHomePanel(com.exam.view.teacher.TeacherMainFrame mainFrame, User teacher) {
        this.mainFrame = mainFrame;
        this.questionService = new QuestionService();
        this.paperService = new PaperService();
        this.teacher = teacher;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(Color.WHITE);

        // 欢迎横幅
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(new Color(240, 248, 255));
        bannerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 220, 240)),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        bannerPanel.setPreferredSize(new Dimension(0, 160));

        JPanel welcomeContent = new JPanel();
        welcomeContent.setLayout(new BoxLayout(welcomeContent, BoxLayout.Y_AXIS));
        welcomeContent.setBackground(new Color(240, 248, 255));

        JLabel welcomeTitle = new JLabel("欢迎使用考试系统");
        welcomeTitle.setFont(new Font("微软雅黑", Font.BOLD, 32));
        welcomeTitle.setForeground(UIUtil.PRIMARY_COLOR);
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeDesc = new JLabel("亲爱的 " + teacher.getRealName() + " 老师，祝您工作顺利！");
        welcomeDesc.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        welcomeDesc.setForeground(new Color(100, 100, 100));
        welcomeDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomeContent.add(Box.createVerticalGlue());
        welcomeContent.add(welcomeTitle);
        welcomeContent.add(Box.createVerticalStrut(15));
        welcomeContent.add(welcomeDesc);
        welcomeContent.add(Box.createVerticalGlue());

        bannerPanel.add(welcomeContent, BorderLayout.CENTER);
        add(bannerPanel, BorderLayout.NORTH);

        // 功能区域
        JPanel functionPanel = createFunctionPanel();
        add(functionPanel, BorderLayout.CENTER);
    }

    private JPanel createFunctionPanel() {
        JPanel functionPanel = new JPanel(new GridBagLayout());
        functionPanel.setBackground(Color.WHITE);
        functionPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;

        // 创建功能卡片
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        functionPanel.add(createFunctionCard("题库管理", "管理所有考试题目", new Color(52, 152, 219), "question"), gbc);

        gbc.gridx = 1;
        functionPanel.add(createFunctionCard("试卷管理", "创建和管理考试试卷", new Color(46, 204, 113), "paper"), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        functionPanel.add(createFunctionCard("导入题目", "从文件导入题目", new Color(155, 89, 182), "import"), gbc);

        gbc.gridx = 1;
        functionPanel.add(createFunctionCard("数据统计", "查看教学统计数据", new Color(231, 76, 60), "home"), gbc);

        return functionPanel;
    }

    private JPanel createFunctionCard(String title, String description, Color color, String viewType) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(30, 25, 30, 25)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 文本内容
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        textPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(color);

        JLabel descLabel = new JLabel(description, SwingConstants.CENTER);
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        descLabel.setForeground(new Color(100, 100, 100));

        textPanel.add(titleLabel);
        textPanel.add(descLabel);

        card.add(textPanel, BorderLayout.CENTER);

        // 添加点击事件
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (mainFrame != null) {
                    mainFrame.switchToView(viewType);
                }
            }
        });

        return card;
    }
}