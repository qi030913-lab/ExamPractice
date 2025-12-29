package com.exam.view;

import com.exam.util.UIUtil;
import com.exam.view.student.StudentLoginFrame;
import com.exam.view.teacher.TeacherLoginFrame;

import javax.swing.*;
import java.awt.*;

/**
 * 角色选择界面
 * 用户首先选择登录角色（学生或教师）
 */
public class RoleSelectionFrame extends JFrame {

    public RoleSelectionFrame() {
        initComponents();
        setTitle("小考试系统 - 角色选择");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        UIUtil.centerWindow(this);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(230, 240, 250));

        // 顶部标题
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(new Color(230, 240, 250));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));

        JLabel titleLabel = new JLabel("小考试系统");
        titleLabel.setFont(new Font("SimHei", Font.BOLD, 28));
        titleLabel.setForeground(new Color(25, 118, 210));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(titleLabel);

        JLabel subTitleLabel = new JLabel("ONLINE EXAM SYSTEM");
        subTitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subTitleLabel.setForeground(new Color(66, 133, 244));
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(Box.createVerticalStrut(10));
        logoPanel.add(subTitleLabel);

        JLabel promptLabel = new JLabel("请选择您的角色");
        promptLabel.setFont(new Font("SimHei", Font.PLAIN, 16));
        promptLabel.setForeground(new Color(60, 60, 60));
        promptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(Box.createVerticalStrut(20));
        logoPanel.add(promptLabel);

        mainPanel.add(logoPanel, BorderLayout.NORTH);

        // 中间角色选择面板
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(230, 240, 250));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        buttonPanel.setBackground(new Color(230, 240, 250));
        buttonPanel.setPreferredSize(new Dimension(450, 120));

        // 学生登录按钮
        JButton studentButton = createRoleButton("学生登录", new Color(33, 150, 243));
        studentButton.addActionListener(e -> openStudentLogin());
        buttonPanel.add(studentButton);

        // 教师登录按钮
        JButton teacherButton = createRoleButton("教师登录", new Color(76, 175, 80));
        teacherButton.addActionListener(e -> openTeacherLogin());
        buttonPanel.add(teacherButton);

        centerPanel.add(buttonPanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 底部提示
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(230, 240, 250));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        JLabel footerLabel = new JLabel("请根据您的身份选择对应的登录入口");
        footerLabel.setFont(new Font("SimHei", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(120, 120, 120));
        footerPanel.add(footerLabel);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * 创建角色按钮
     */
    private JButton createRoleButton(String text, Color bgColor) {
        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setLayout(new BoxLayout(buttonWrapper, BoxLayout.Y_AXIS));
        buttonWrapper.setOpaque(false);

        JButton button = new JButton(text);
        button.setFont(new Font("SimHei", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(200, 120));

        // 鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    /**
     * 打开学生登录界面
     */
    private void openStudentLogin() {
        SwingUtilities.invokeLater(() -> {
            StudentLoginFrame studentLoginFrame = new StudentLoginFrame();
            studentLoginFrame.setVisible(true);
            dispose();
        });
    }

    /**
     * 打开教师登录界面
     */
    private void openTeacherLogin() {
        SwingUtilities.invokeLater(() -> {
            TeacherLoginFrame teacherLoginFrame = new TeacherLoginFrame();
            teacherLoginFrame.setVisible(true);
            dispose();
        });
    }
}
