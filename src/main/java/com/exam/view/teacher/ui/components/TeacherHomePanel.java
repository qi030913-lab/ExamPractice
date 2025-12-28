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

    public TeacherHomePanel(com.exam.view.teacher.TeacherMainFrame mainFrame, User teacher) {
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

        // 统计卡片区域
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.CENTER);
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 30, 30));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        try {
            int totalQuestions = questionService.getAllQuestions().size();
            int totalPapers = paperService.getAllPapers().size();
            int publishedPapers = paperService.getAllPublishedPapers().size();
            int unpublishedPapers = totalPapers - publishedPapers;

            // 创建统计卡片
            statsPanel.add(createStatCard("📊", "题目总数", String.valueOf(totalQuestions), new Color(52, 152, 219)));
            statsPanel.add(createStatCard("📋", "试卷总数", String.valueOf(totalPapers), new Color(46, 204, 113)));
            statsPanel.add(createStatCard("✅", "已发布试卷", String.valueOf(publishedPapers), new Color(155, 89, 182)));
            statsPanel.add(createStatCard("❌", "未发布试卷", String.valueOf(unpublishedPapers), new Color(231, 76, 60)));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return statsPanel;
    }

    private JPanel createStatCard(String icon, String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(30, 25, 30, 25)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 图标
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("微软雅黑", Font.PLAIN, 32));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // 标题和值
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        textPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        titleLabel.setForeground(new Color(100, 100, 100));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        valueLabel.setForeground(color);

        textPanel.add(titleLabel);
        textPanel.add(valueLabel);

        card.add(iconLabel, BorderLayout.NORTH);
        card.add(textPanel, BorderLayout.CENTER);

        // 悬停效果
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(248, 249, 250));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color, 2),
                        BorderFactory.createEmptyBorder(30, 25, 30, 25)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                        BorderFactory.createEmptyBorder(30, 25, 30, 25)
                ));
            }
        });

        return card;
    }
}