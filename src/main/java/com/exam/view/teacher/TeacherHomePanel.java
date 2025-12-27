package com.exam.view.teacher;

import com.exam.model.User;
import com.exam.util.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 教师端 - 首页面板
 * 显示欢迎信息和功能卡片
 */
public class TeacherHomePanel extends JPanel {
    private final TeacherMainFrame mainFrame;
    private final User teacher;

    public TeacherHomePanel(TeacherMainFrame mainFrame, User teacher) {
        this.mainFrame = mainFrame;
        this.teacher = teacher;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(Color.WHITE);

        // 欢迎横幅
        add(createBannerPanel(), BorderLayout.NORTH);

        // 功能卡片
        add(createContentPanel(), BorderLayout.CENTER);
    }

    private JPanel createBannerPanel() {
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

        JLabel welcomeTitle = new JLabel("教师管理系统");
        welcomeTitle.setFont(new Font("微软雅黑", Font.BOLD, 32));
        welcomeTitle.setForeground(UIUtil.PRIMARY_COLOR);
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeDesc = new JLabel("尊敬的 " + teacher.getRealName() + " 老师，欢迎回来！");
        welcomeDesc.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        welcomeDesc.setForeground(new Color(100, 100, 100));
        welcomeDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomeContent.add(Box.createVerticalGlue());
        welcomeContent.add(welcomeTitle);
        welcomeContent.add(Box.createVerticalStrut(15));
        welcomeContent.add(welcomeDesc);
        welcomeContent.add(Box.createVerticalGlue());

        bannerPanel.add(welcomeContent, BorderLayout.CENTER);

        return bannerPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new GridLayout(2, 2, 30, 30));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // 创建功能卡片
        contentPanel.add(createFeatureCard("题库管理", "管理试题库", UIUtil.PRIMARY_COLOR, "question"));
        contentPanel.add(createFeatureCard("试卷管理", "创建和管理试卷", UIUtil.SUCCESS_COLOR, "paper"));
        contentPanel.add(createFeatureCard("学生管理", "查看学生信息", UIUtil.WARNING_COLOR, null));
        contentPanel.add(createFeatureCard("成绩统计", "分析考试成绩", UIUtil.DANGER_COLOR, null));

        return contentPanel;
    }

    private JPanel createFeatureCard(String title, String desc, Color color, String targetView) {
        JPanel card = new JPanel(new BorderLayout(10, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(30, 25, 30, 25)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        textPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(color);

        JLabel descLabel = new JLabel(desc, SwingConstants.CENTER);
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        descLabel.setForeground(UIUtil.TEXT_GRAY);

        textPanel.add(titleLabel);
        textPanel.add(descLabel);

        card.add(textPanel, BorderLayout.CENTER);

        // 添加悬停效果
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
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (targetView != null) {
                    mainFrame.switchToView(targetView);
                } else {
                    UIUtil.showInfo(mainFrame, "功能开发中...");
                }
            }
        });

        return card;
    }
}
