package com.exam.view.student;

import com.exam.model.User;
import com.exam.service.ExamService;
import com.exam.service.PaperService;
import com.exam.util.UIUtil;
import com.exam.util.IconUtil;
// import com.exam.view.LoginFrame; // 已删除,使用StudentLoginFrame
import com.exam.view.student.ui.components.StudentExamPanel;
import com.exam.view.student.ui.components.StudentNetworkPanel;
import com.exam.view.student.ui.components.StudentScorePanel;
import com.exam.view.student.ui.components.StudentAchievementPanel;
import javax.swing.*;
import java.awt.*;

/**
 * 学生主界面 - 主框架
 * 
 * 功能说明：
 * 1. 提供顶部导航栏（Logo、用户信息、退出登录）
 * 2. 提供左侧菜单栏（我的主页、考试题库、成绩查询、我的成就）
 * 3. 管理视图切换逻辑
 * 4. 各功能模块已拆分为独立面板类：
 *    - StudentExamPanel: 考试列表
 *    - StudentScorePanel: 成绩查询
 *    - StudentAchievementPanel: 我的成就
 * 
 * @author 系统管理员
 * @version 2.0 (重构版本 - 清理重复代码)
 */
public class StudentMainFrame extends JFrame {
    private final User student;
    private JPanel mainContentPanel;
    private String currentView = "home";
    private java.util.List<JButton> menuButtons = new java.util.ArrayList<>();
    
    // 添加一个全局变量来保存当前选择的科目
    private String currentExamSubject = "全部";
    
    // 添加考试面板的引用，用于刷新
    private StudentExamPanel examPanel;
    
    // 保持网络通信面板的引用，避免切换页面时断开连接
    private StudentNetworkPanel networkPanel;
    
    // 添加定时器用于定期刷新试卷列表
    private javax.swing.Timer refreshTimer;
    
    // 记录上次检查的已发布试卷数量，用于优化刷新
    private int lastPublishedPaperCount = 0;
    
    public StudentMainFrame(User student) {
        this.student = student;
        initComponents();
        setTitle("小考试系统 - 学生端");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 设置窗口图标
        try {
            setIconImage(new ImageIcon(getClass().getClassLoader().getResource("pic/logo.png")).getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        UIUtil.centerWindow(this);
        
        // 添加窗口监听器以处理关闭事件
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopAutoRefreshTimer();
                // 关闭程序时断开网络连接
                if (networkPanel != null) {
                    networkPanel.disconnect();
                }
                dispose();
            }
        });
        
        // 启动定时器，每30秒检查一次是否有新发布的试卷
        startAutoRefreshTimer();
    }
    
    /**
     * 启动自动刷新定时器
     */
    private void startAutoRefreshTimer() {
        // 创建一个定时器，每30秒刷新一次考试列表
        refreshTimer = new javax.swing.Timer(30000, e -> {
            if ("exam".equals(currentView)) {
                // 检查已发布试卷数量是否有变化，如果有变化则刷新
                checkAndRefreshExamList();
            }
        });
        refreshTimer.start();
    }
    
    /**
     * 停止自动刷新定时器
     */
    private void stopAutoRefreshTimer() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
    
    /**
     * 设置当前考试科目
     */
    public void setCurrentExamSubject(String subject) {
        this.currentExamSubject = subject;
    }
    
    /**
     * 刷新考试列表
     */
    public void refreshExamList() {
        if (examPanel != null) {
            examPanel.refreshExamList();
        }
    }
    
    /**
     * 手动检查并刷新考试列表
     */
    public void manualRefreshExamList() {
        checkAndRefreshExamList();
    }
    
    /**
     * 检查试卷列表是否有更新，如有则刷新
     */
    private void checkAndRefreshExamList() {
        try {
            com.exam.service.PaperService paperService = new com.exam.service.PaperService();
            java.util.List<com.exam.model.Paper> publishedPapers = paperService.getAllPublishedPapers();
            int currentPaperCount = publishedPapers.size();
            
            // 如果试卷数量发生变化，则刷新列表
            if (currentPaperCount != lastPublishedPaperCount) {
                lastPublishedPaperCount = currentPaperCount;
                refreshExamList();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 即使检查失败也不影响正常功能，继续运行
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(UIUtil.BACKGROUND_COLOR);

        // 顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 30, 15, 30)
        ));

        // 左侧：Logo和标题
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setBackground(Color.WHITE);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 0));
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("小考试系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
        JLabel versionLabel = new JLabel("版本：v1.0");
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        versionLabel.setForeground(UIUtil.TEXT_GRAY);
        titlePanel.add(titleLabel);
        titlePanel.add(versionLabel);
        leftPanel.add(titlePanel);

        topPanel.add(leftPanel, BorderLayout.WEST);

        // 右侧：用户信息和退出
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        rightPanel.setBackground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("欢迎，" + student.getRealName() + " 同学");
        welcomeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        welcomeLabel.setForeground(UIUtil.TEXT_COLOR);
        rightPanel.add(welcomeLabel);

        JButton logoutButton = new JButton("退出登录");
        logoutButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(UIUtil.TEXT_COLOR);
        logoutButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logout());
        rightPanel.add(logoutButton);

        topPanel.add(rightPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 主内容区域
        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(UIUtil.BACKGROUND_COLOR);

        // 左侧导航栏
        JPanel sidebarPanel = createSidebarPanel();
        contentPanel.add(sidebarPanel, BorderLayout.WEST);

        // 右侧内容区
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(UIUtil.BACKGROUND_COLOR);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainContentPanel.add(createHomePanel(), BorderLayout.CENTER);

        contentPanel.add(mainContentPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        // 用户信息区域
        JPanel userPanel = new JPanel(new BorderLayout(15, 0));
        userPanel.setBackground(new Color(245, 250, 255));
        userPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 230, 240)),
            BorderFactory.createEmptyBorder(30, 35, 30, 20)
        ));

        // 头像区域
        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
        avatarPanel.setBackground(new Color(245, 250, 255));

        JPanel avatarCircle = new JPanel(new GridBagLayout());
        avatarCircle.setPreferredSize(new Dimension(60, 60));
        avatarCircle.setMaximumSize(new Dimension(60, 60));
        avatarCircle.setBackground(UIUtil.PRIMARY_COLOR);
        avatarCircle.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 240), 2));

        // 加载学生头像图片
        JLabel userIconLabel = new JLabel();
        try {
            ImageIcon avatarIcon = new ImageIcon(getClass().getClassLoader().getResource("pic/stu.jpg"));
            Image scaledImage = avatarIcon.getImage().getScaledInstance(56, 56, Image.SCALE_SMOOTH);
            userIconLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            // 如果加载图片失败，使用默认emoji
            userIconLabel.setText("👤");
            userIconLabel.setFont(new Font("微软雅黑", Font.PLAIN, 32));
        }
        avatarCircle.add(userIconLabel);
        avatarPanel.add(avatarCircle);
        userPanel.add(avatarPanel, BorderLayout.WEST);

        // 用户信息
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(new Color(245, 250, 255));

        JLabel userNameLabel = new JLabel(student.getRealName());
        userNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        userNameLabel.setForeground(new Color(34, 34, 34));
        userNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rolePanel.setBackground(new Color(245, 250, 255));
        rolePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel("学生");
        roleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setBackground(UIUtil.PRIMARY_COLOR);
        roleLabel.setOpaque(true);
        roleLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        rolePanel.add(roleLabel);

        JLabel idLabel = new JLabel("学号: " + student.getStudentNumber());
        idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        idLabel.setForeground(new Color(120, 120, 120));
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userInfoPanel.add(userNameLabel);
        userInfoPanel.add(Box.createVerticalStrut(8));
        userInfoPanel.add(rolePanel);
        userInfoPanel.add(Box.createVerticalStrut(5));
        userInfoPanel.add(idLabel);

        userPanel.add(userInfoPanel, BorderLayout.CENTER);
        Dimension pref = userPanel.getPreferredSize();
        userPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));

        sidebar.add(userPanel);

        // 分割线
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(240, 1));
        separator.setForeground(new Color(240, 240, 240));
        sidebar.add(separator);

        // 导航菜单
        String[][] menuConfig = {
            {"home", "我的主页"},
            {"exam", "考试题库"},
            {"score", "成绩查询"},
            {"achievement", "我的成就"},
            {"network", "网络通信"}
        };

        for (int i = 0; i < menuConfig.length; i++) {
            String view = menuConfig[i][0];
            String text = menuConfig[i][1];

            JButton menuButton = createSidebarButton(text, view, i == 0);
            menuButton.addActionListener(e -> switchView(view));

            menuButtons.add(menuButton);
            sidebar.add(menuButton);
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JButton createSidebarButton(String text, String view, boolean isActive) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setPreferredSize(new Dimension(240, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        
        Icon icon = getMenuIcon(view, isActive ? UIUtil.PRIMARY_COLOR : new Color(120, 120, 120), 16);
        button.setIcon(icon);
        button.setIconTextGap(10);

        updateButtonStyle(button, isActive);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!isButtonActive(button)) {
                    button.setBackground(new Color(248, 249, 250));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!isButtonActive(button)) {
                    button.setBackground(Color.WHITE);
                }
            }
        });

        return button;
    }

    private Icon getMenuIcon(String view, Color color, int size) {
        switch (view) {
            case "home":
                return IconUtil.createHomeIcon(color, size);
            case "exam":
                return IconUtil.createDocumentIcon(color, size);
            case "score":
                return IconUtil.createChartIcon(color, size);
            case "achievement":
                return IconUtil.createTrophyIcon(color, size);
            case "network":
                return IconUtil.createNetworkIcon(color, size);
            default:
                return IconUtil.createCircleIcon(color, size);
        }
    }
    
    private boolean isButtonActive(JButton button) {
        return button.getBackground().equals(new Color(240, 248, 255));
    }

    private void updateButtonStyle(JButton button, boolean isActive) {
        if (isActive) {
            button.setBackground(new Color(240, 248, 255));
            button.setForeground(UIUtil.PRIMARY_COLOR);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, UIUtil.PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(12, 17, 12, 10)
            ));
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(51, 51, 51));
            button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
        }
    }
    
    private void updateButtonIcon(JButton button, String view, boolean isActive) {
        Color iconColor = isActive ? UIUtil.PRIMARY_COLOR : new Color(120, 120, 120);
        Icon icon = getMenuIcon(view, iconColor, 16);
        button.setIcon(icon);
    }

    private void switchView(String view) {
        if (currentView.equals(view)) {
            return;
        }

        currentView = view;

        // 更新所有按钮的状态
        String[] views = {"home", "exam", "score", "achievement", "network"};
        for (int i = 0; i < menuButtons.size(); i++) {
            JButton button = menuButtons.get(i);
            boolean isActive = i == getViewIndex(view);
            updateButtonStyle(button, isActive);
            updateButtonIcon(button, views[i], isActive);
        }

        // 切换内容
        mainContentPanel.removeAll();

        switch (view) {
            case "home":
                mainContentPanel.add(createHomePanel(), BorderLayout.CENTER);
                break;
            case "exam":
                // 传递当前考试科目和主框架给考试面板
                examPanel = new StudentExamPanel(student, currentExamSubject, this);
                mainContentPanel.add(examPanel, BorderLayout.CENTER);
                
                // 当切换到考试页面时，立即检查是否有新发布的试卷
                SwingUtilities.invokeLater(() -> checkAndRefreshExamList());
                break;
            case "score":
                mainContentPanel.add(new StudentScorePanel(student), BorderLayout.CENTER);
                break;
            case "achievement":
                mainContentPanel.add(new StudentAchievementPanel(student), BorderLayout.CENTER);
                break;
            case "network":
                // 复用网络通信面板实例，避免切换页面时断开连接
                if (networkPanel == null) {
                    networkPanel = new StudentNetworkPanel(student);
                }
                mainContentPanel.add(networkPanel, BorderLayout.CENTER);
                break;
            default:
                mainContentPanel.add(createHomePanel(), BorderLayout.CENTER);
        }

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private int getViewIndex(String view) {
        switch (view) {
            case "home": return 0;
            case "exam": return 1;
            case "score": return 2;
            case "achievement": return 3;
            case "network": return 4;
            default: return -1;
        }
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);

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

        JLabel welcomeDesc = new JLabel("亲爱的 " + student.getRealName() + " 同学，祝您学习进步！");
        welcomeDesc.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        welcomeDesc.setForeground(new Color(100, 100, 100));
        welcomeDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomeContent.add(Box.createVerticalGlue());
        welcomeContent.add(welcomeTitle);
        welcomeContent.add(Box.createVerticalStrut(15));
        welcomeContent.add(welcomeDesc);
        welcomeContent.add(Box.createVerticalGlue());

        bannerPanel.add(welcomeContent, BorderLayout.CENTER);
        panel.add(bannerPanel, BorderLayout.NORTH);

        // 主内容区
        JPanel contentPanel = new JPanel(new GridLayout(2, 2, 30, 30));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // 创建功能卡片
        contentPanel.add(createFeatureCard("在线考试", "开始你的考试之旅", UIUtil.PRIMARY_COLOR, "exam"));
        contentPanel.add(createFeatureCard("成绩查询", "查看你的考试成绩", UIUtil.SUCCESS_COLOR, "score"));
        contentPanel.add(createFeatureCard("题库练习", "刷题提升能力", UIUtil.WARNING_COLOR, "exam"));
        contentPanel.add(createFeatureCard("我的成就", "查看学习成果", UIUtil.DANGER_COLOR, "achievement"));

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
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
                    switchView(targetView);
                }
            }
        });

        return card;
    }
    
    private void logout() {
        if (UIUtil.showConfirm(this, "确定要退出登录吗?")) {
            // 退出登录时断开网络连接
            if (networkPanel != null) {
                networkPanel.disconnect();
            }
            dispose();
            new StudentLoginFrame().setVisible(true);
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        stopAutoRefreshTimer();
        super.finalize();
    }
}
