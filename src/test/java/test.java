//package com.exam.view;
//
//import com.exam.model.User;
//import com.exam.util.UIUtil;
//import com.exam.util.IconUtil;
//import com.exam.view.teacher.TeacherQuestionPanel;
//import com.exam.view.teacher.TeacherImportPanel;
//import com.exam.view.teacher.TeacherPaperPanel;
//import javax.swing.*;
//import java.awt.*;
//import java.util.List;
//import java.util.ArrayList;
//
///**
// * 教师主界面 - 主框架
// *
// * 功能说明：
// * 1. 提供顶部导航栏（Logo、用户信息、退出登录）
// * 2. 提供左侧菜单栏（我的主页、题库管理、试卷管理、导入题目）
// * 3. 管理视图切换逻辑
// * 4. 各功能模块已拆分为独立面板类：
// *    - TeacherQuestionPanel: 题库管理
// *    - TeacherPaperPanel: 试卷管理
// *    - TeacherImportPanel: 导入题目
// *
// * @author 系统管理员
// * @version 2.0 (重构版本 - 清理重复代码)
// */
//public class test extends JFrame {
//    private final User teacher;
//    private JPanel mainContentPanel;
//    private String currentView = "home";
//    private List<JButton> menuButtons = new ArrayList<>();
//
//    public TeacherMainFrame(User teacher) {
//        this.teacher = teacher;
//        initComponents();
//        setTitle("未来教育考试系统 - 教师端");
//        setSize(1200, 700);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        UIUtil.centerWindow(this);
//    }
//
//    private void initComponents() {
//        setLayout(new BorderLayout(0, 0));
//        getContentPane().setBackground(UIUtil.BACKGROUND_COLOR);
//
//        // 顶部面板
//        JPanel topPanel = new JPanel(new BorderLayout());
//        topPanel.setBackground(Color.WHITE);
//        topPanel.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
//                BorderFactory.createEmptyBorder(15, 30, 15, 30)
//        ));
//
//        // 左侧：Logo和标题
//        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
//        leftPanel.setBackground(Color.WHITE);
//
//        JLabel logoLabel = new JLabel("🐬");
//        logoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 32));
//        leftPanel.add(logoLabel);
//
//        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 0));
//        titlePanel.setBackground(Color.WHITE);
//        JLabel titleLabel = new JLabel("未来教育●考试系统");
//        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
//        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
//        JLabel versionLabel = new JLabel("版本：4.0.0.92");
//        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
//        versionLabel.setForeground(UIUtil.TEXT_GRAY);
//        titlePanel.add(titleLabel);
//        titlePanel.add(versionLabel);
//        leftPanel.add(titlePanel);
//
//        topPanel.add(leftPanel, BorderLayout.WEST);
//
//        // 右侧：用户信息和退出
//        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
//        rightPanel.setBackground(Color.WHITE);
//
//        JLabel welcomeLabel = new JLabel("欢迎，" + teacher.getRealName() + " 老师");
//        welcomeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
//        welcomeLabel.setForeground(UIUtil.TEXT_COLOR);
//        rightPanel.add(welcomeLabel);
//
//        JButton logoutButton = new JButton("退出登录");
//        logoutButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
//        logoutButton.setBackground(Color.WHITE);
//        logoutButton.setForeground(UIUtil.TEXT_COLOR);
//        logoutButton.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(new Color(220, 220, 220)),
//                BorderFactory.createEmptyBorder(5, 15, 5, 15)
//        ));
//        logoutButton.setFocusPainted(false);
//        logoutButton.addActionListener(e -> logout());
//        rightPanel.add(logoutButton);
//
//        topPanel.add(rightPanel, BorderLayout.EAST);
//        add(topPanel, BorderLayout.NORTH);
//
//        // 主内容区域 - 左侧导航 + 右侧内容
//        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
//        contentPanel.setBackground(UIUtil.BACKGROUND_COLOR);
//
//        // 左侧导航栏
//        JPanel sidebarPanel = createSidebarPanel();
//        contentPanel.add(sidebarPanel, BorderLayout.WEST);
//
//        // 右侧内容区
//        mainContentPanel = new JPanel(new BorderLayout());
//        mainContentPanel.setBackground(UIUtil.BACKGROUND_COLOR);
//        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//        mainContentPanel.add(createHomePanel(), BorderLayout.CENTER);
//
//        contentPanel.add(mainContentPanel, BorderLayout.CENTER);
//        add(contentPanel, BorderLayout.CENTER);
//    }
//
//    private JPanel createSidebarPanel() {
//        JPanel sidebar = new JPanel();
//        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
//        sidebar.setBackground(Color.WHITE);
//        sidebar.setPreferredSize(new Dimension(180, 0));
//        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));
//
//        // 用户信息区域
//        JPanel userPanel = new JPanel(new BorderLayout(15, 0));
//        userPanel.setBackground(new Color(245, 250, 255));
//        userPanel.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 230, 240)),
//                BorderFactory.createEmptyBorder(30, 0, 30, 20)
//        ));
//
//        // 头像区域
//        JPanel avatarPanel = new JPanel();
//        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
//        avatarPanel.setBackground(new Color(245, 250, 255));
//
//        JPanel avatarCircle = new JPanel(new GridBagLayout());
//        avatarCircle.setPreferredSize(new Dimension(60, 60));
//        avatarCircle.setMaximumSize(new Dimension(60, 60));
//        avatarCircle.setBackground(UIUtil.PRIMARY_COLOR);
//        avatarCircle.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 240), 2));
//
//        JLabel userIconLabel = new JLabel("👨‍🏫");
//        userIconLabel.setFont(new Font("微软雅黑", Font.PLAIN, 28));
//        avatarCircle.add(userIconLabel);
//        avatarPanel.add(avatarCircle);
//        userPanel.add(avatarPanel, BorderLayout.WEST);
//
//        // 用户信息
//        JPanel userInfoPanel = new JPanel();
//        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
//        userInfoPanel.setBackground(new Color(245, 250, 255));
//
//        JLabel userNameLabel = new JLabel(teacher.getRealName());
//        userNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
//        userNameLabel.setForeground(new Color(34, 34, 34));
//        userNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
//        rolePanel.setBackground(new Color(245, 250, 255));
//        rolePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        JLabel roleLabel = new JLabel("教师");
//        roleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
//        roleLabel.setForeground(Color.WHITE);
//        roleLabel.setBackground(new Color(231, 76, 60));
//        roleLabel.setOpaque(true);
//        roleLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
//        rolePanel.add(roleLabel);
//
//        JLabel idLabel = new JLabel("ID: " + teacher.getUserId());
//        idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
//        idLabel.setForeground(new Color(120, 120, 120));
//        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        userInfoPanel.add(userNameLabel);
//        userInfoPanel.add(Box.createVerticalStrut(8));
//        userInfoPanel.add(rolePanel);
//        userInfoPanel.add(Box.createVerticalStrut(5));
//        userInfoPanel.add(idLabel);
//
//        userPanel.add(userInfoPanel, BorderLayout.CENTER);
//        Dimension pref = userPanel.getPreferredSize();
//        userPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
//
//        sidebar.add(userPanel);
//
//        // 分割线
//        JSeparator separator = new JSeparator();
//        separator.setMaximumSize(new Dimension(180, 1));
//        separator.setForeground(new Color(240, 240, 240));
//        sidebar.add(separator);
//
//        // 导航菜单
//        String[][] menuConfig = {
//                {"home", "我的主页"},
//                {"question", "题库管理"},
//                {"paper", "试卷管理"},
//                {"import", "导入题目"}
//        };
//
//        for (int i = 0; i < menuConfig.length; i++) {
//            String view = menuConfig[i][0];
//            String text = menuConfig[i][1];
//
//            JButton menuButton = createSidebarButton(text, view, i == 0);
//            menuButton.addActionListener(e -> switchView(view));
//
//            menuButtons.add(menuButton);
//            sidebar.add(menuButton);
//        }
//        sidebar.add(Box.createVerticalGlue());
//        return sidebar;
//    }
//
//    private JButton createSidebarButton(String text, String view, boolean isActive) {
//        JButton button = new JButton(text);
//        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
//        button.setHorizontalAlignment(SwingConstants.LEFT);
//        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 0));
//        button.setFocusPainted(false);
//        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
//        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
//        button.setContentAreaFilled(false);
//        button.setOpaque(true);
//
//        Icon icon = getMenuIcon(view, isActive ? UIUtil.PRIMARY_COLOR : new Color(120, 120, 120), 16);
//        button.setIcon(icon);
//        button.setIconTextGap(10);
//
//        updateButtonStyle(button, isActive);
//
//        button.addMouseListener(new java.awt.event.MouseAdapter() {
//            @Override
//            public void mouseEntered(java.awt.event.MouseEvent evt) {
//                if (!isButtonActive(button)) {
//                    button.setBackground(new Color(248, 249, 250));
//                }
//            }
//
//            @Override
//            public void mouseExited(java.awt.event.MouseEvent evt) {
//                if (!isButtonActive(button)) {
//                    button.setBackground(Color.WHITE);
//                }
//            }
//        });
//
//        return button;
//    }
//
//    private Icon getMenuIcon(String view, Color color, int size) {
//        switch (view) {
//            case "home":
//                return IconUtil.createHomeIcon(color, size);
//            case "question":
//                return IconUtil.createDocumentIcon(color, size);
//            case "paper":
//                return IconUtil.createChartIcon(color, size);
//            case "import":
//                return IconUtil.createUploadIcon(color, size);
//            default:
//                return IconUtil.createCircleIcon(color, size);
//        }
//    }
//
//    private boolean isButtonActive(JButton button) {
//        return button.getBackground().equals(new Color(240, 248, 255));
//    }
//
//    private void updateButtonStyle(JButton button, boolean isActive) {
//        if (isActive) {
//            button.setBackground(new Color(240, 248, 255));
//            button.setForeground(UIUtil.PRIMARY_COLOR);
//            button.setBorder(BorderFactory.createCompoundBorder(
//                    BorderFactory.createMatteBorder(0, 3, 0, 0, UIUtil.PRIMARY_COLOR),
//                    BorderFactory.createEmptyBorder(12, 17, 12, 10)
//            ));
//        } else {
//            button.setBackground(Color.WHITE);
//            button.setForeground(new Color(51, 51, 51));
//            button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 0));
//        }
//    }
//
//    private void updateButtonIcon(JButton button, String view, boolean isActive) {
//        Color iconColor = isActive ? UIUtil.PRIMARY_COLOR : new Color(120, 120, 120);
//        Icon icon = getMenuIcon(view, iconColor, 16);
//        button.setIcon(icon);
//    }
//
//    private void switchView(String view) {
//        if (currentView.equals(view)) {
//            return;
//        }
//
//        currentView = view;
//
//        // 更新所有按钮的状态
//        String[] views = {"home", "question", "paper", "import"};
//        for (int i = 0; i < menuButtons.size(); i++) {
//            JButton button = menuButtons.get(i);
//            boolean isActive = i == getViewIndex(view);
//            updateButtonStyle(button, isActive);
//            updateButtonIcon(button, views[i], isActive);
//        }
//
//        // 切换内容
//        mainContentPanel.removeAll();
//
//        switch (view) {
//            case "home":
//                mainContentPanel.add(createHomePanel(), BorderLayout.CENTER);
//                break;
//            case "question":
//                mainContentPanel.add(new TeacherQuestionPanel(teacher), BorderLayout.CENTER);
//                break;
//            case "paper":
//                mainContentPanel.add(new TeacherPaperPanel(teacher), BorderLayout.CENTER);
//                break;
//            case "import":
//                mainContentPanel.add(new TeacherImportPanel(teacher), BorderLayout.CENTER);
//                break;
//            default:
//                mainContentPanel.add(createHomePanel(), BorderLayout.CENTER);
//        }
//
//        mainContentPanel.revalidate();
//        mainContentPanel.repaint();
//    }
//
//    private int getViewIndex(String view) {
//        switch (view) {
//            case "home": return 0;
//            case "question": return 1;
//            case "paper": return 2;
//            case "import": return 3;
//            default: return -1;
//        }
//    }
//
//    private JPanel createHomePanel() {
//        JPanel panel = new JPanel(new BorderLayout(0, 20));
//        panel.setBackground(Color.WHITE);
//
//        // 欢迎横幅
//        JPanel bannerPanel = new JPanel(new BorderLayout());
//        bannerPanel.setBackground(new Color(240, 248, 255));
//        bannerPanel.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 220, 240)),
//                BorderFactory.createEmptyBorder(40, 40, 40, 40)
//        ));
//        bannerPanel.setPreferredSize(new Dimension(0, 160));
//
//        JPanel welcomeContent = new JPanel();
//        welcomeContent.setLayout(new BoxLayout(welcomeContent, BoxLayout.Y_AXIS));
//        welcomeContent.setBackground(new Color(240, 248, 255));
//
//        JLabel welcomeTitle = new JLabel("教师管理系统");
//        welcomeTitle.setFont(new Font("微软雅黑", Font.BOLD, 32));
//        welcomeTitle.setForeground(UIUtil.PRIMARY_COLOR);
//        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//        JLabel welcomeDesc = new JLabel("尊敬的 " + teacher.getRealName() + " 老师，欢迎回来！");
//        welcomeDesc.setFont(new Font("微软雅黑", Font.PLAIN, 16));
//        welcomeDesc.setForeground(new Color(100, 100, 100));
//        welcomeDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//        welcomeContent.add(Box.createVerticalGlue());
//        welcomeContent.add(welcomeTitle);
//        welcomeContent.add(Box.createVerticalStrut(15));
//        welcomeContent.add(welcomeDesc);
//        welcomeContent.add(Box.createVerticalGlue());
//
//        bannerPanel.add(welcomeContent, BorderLayout.CENTER);
//        panel.add(bannerPanel, BorderLayout.NORTH);
//
//        // 主内容区
//        JPanel contentPanel = new JPanel(new GridLayout(2, 2, 30, 30));
//        contentPanel.setBackground(Color.WHITE);
//        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
//
//        // 创建功能卡片
//        contentPanel.add(createFeatureCard("题库管理", "管理试题库", UIUtil.PRIMARY_COLOR, "question"));
//        contentPanel.add(createFeatureCard("试卷管理", "创建和管理试卷", UIUtil.SUCCESS_COLOR, "paper"));
//        contentPanel.add(createFeatureCard("学生管理", "查看学生信息", UIUtil.WARNING_COLOR, null));
//        contentPanel.add(createFeatureCard("成绩统计", "分析考试成绩", UIUtil.DANGER_COLOR, null));
//
//        panel.add(contentPanel, BorderLayout.CENTER);
//
//        return panel;
//    }
//
//    private JPanel createFeatureCard(String title, String desc, Color color, String targetView) {
//        JPanel card = new JPanel(new BorderLayout(10, 15));
//        card.setBackground(Color.WHITE);
//        card.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
//                BorderFactory.createEmptyBorder(30, 25, 30, 25)
//        ));
//        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
//
//        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 8));
//        textPanel.setBackground(Color.WHITE);
//
//        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
//        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
//        titleLabel.setForeground(color);
//
//        JLabel descLabel = new JLabel(desc, SwingConstants.CENTER);
//        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        descLabel.setForeground(UIUtil.TEXT_GRAY);
//
//        textPanel.add(titleLabel);
//        textPanel.add(descLabel);
//
//        card.add(textPanel, BorderLayout.CENTER);
//
//        // 添加悬停效果
//        card.addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mouseEntered(java.awt.event.MouseEvent evt) {
//                card.setBackground(new Color(248, 249, 250));
//                card.setBorder(BorderFactory.createCompoundBorder(
//                        BorderFactory.createLineBorder(color, 2),
//                        BorderFactory.createEmptyBorder(30, 25, 30, 25)
//                ));
//            }
//            public void mouseExited(java.awt.event.MouseEvent evt) {
//                card.setBackground(Color.WHITE);
//                card.setBorder(BorderFactory.createCompoundBorder(
//                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
//                        BorderFactory.createEmptyBorder(30, 25, 30, 25)
//                ));
//            }
//            public void mouseClicked(java.awt.event.MouseEvent evt) {
//                if (targetView != null) {
//                    switchView(targetView);
//                } else {
//                    UIUtil.showInfo(TeacherMainFrame.this, "功能开发中...");
//                }
//            }
//        });
//
//        return card;
//    }
//
//    private void logout() {
//        if (UIUtil.showConfirm(this, "确定要退出登录吗？")) {
//            dispose();
//            new LoginFrame().setVisible(true);
//        }
//    }
//}
