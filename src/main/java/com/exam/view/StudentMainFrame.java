package com.exam.view;

import com.exam.model.Paper;
import com.exam.model.User;
import com.exam.model.ExamRecord;
import com.exam.model.AnswerRecord;
import com.exam.service.ExamService;
import com.exam.service.PaperService;
import com.exam.util.UIUtil;
import com.exam.util.IconUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.List;

/**
 * 学生主界面
 */
public class StudentMainFrame extends JFrame {
    private final User student;
    private final PaperService paperService;
    private final ExamService examService;
    private JTable paperTable;
    private DefaultTableModel tableModel;
    private JPanel mainContentPanel;
    private String currentView = "home";
    private java.util.List<JButton> menuButtons = new java.util.ArrayList<>();
    private String currentSubject = "全部"; // 当前选中的科目
    private static final String[] SUBJECTS = {"全部", "Java", "Vue", "数据结构", "马克思主义", "计算机网络", "操作系统", "数据库"};

    public StudentMainFrame(User student) {
        this.student = student;
        this.paperService = new PaperService();
        this.examService = new ExamService();
        initComponents();
        setTitle("未来教育考试系统 - 学生端");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UIUtil.centerWindow(this);
        // loadPapers() 将在 createExamPanel() 中调用
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(UIUtil.BACKGROUND_COLOR);

        // 顶部面板 - 类似图片中的头部
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 30, 15, 30)
        ));

        // 左侧：Logo和标题
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setBackground(Color.WHITE);

        JLabel logoLabel = new JLabel("🐬");
        logoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 32));
        leftPanel.add(logoLabel);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 0));
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("未来教育●考试系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
        JLabel versionLabel = new JLabel("版本：4.0.0.92");
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

        // 主内容区域 - 左侧导航 + 右侧内容
        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(UIUtil.BACKGROUND_COLOR);

        // 左侧导航栏
        JPanel sidebarPanel = createSidebarPanel();
        contentPanel.add(sidebarPanel, BorderLayout.WEST);

        // 右侧内容区
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(UIUtil.BACKGROUND_COLOR);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainContentPanel.add(createHomePanel(), BorderLayout.CENTER); // 默认显示主页

        contentPanel.add(mainContentPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        // 用户信息区域
        JPanel userPanel = new JPanel(new BorderLayout(15, 0));
        userPanel.setBackground(new Color(245, 250, 255));
        userPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 230, 240)),
            BorderFactory.createEmptyBorder(30, 0, 30, 20)
        ));

        // 左侧头像区域
        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
        avatarPanel.setBackground(new Color(245, 250, 255));

        // 头像圆形背景
        JPanel avatarCircle = new JPanel(new GridBagLayout());
        avatarCircle.setPreferredSize(new Dimension(60, 60));
        avatarCircle.setMaximumSize(new Dimension(60, 60));
        avatarCircle.setBackground(UIUtil.PRIMARY_COLOR);
        avatarCircle.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 240), 2));

        JLabel userIconLabel = new JLabel("👤");
        userIconLabel.setFont(new Font("微软雅黑", Font.PLAIN, 32));
        avatarCircle.add(userIconLabel);

        avatarPanel.add(avatarCircle);
        userPanel.add(avatarPanel, BorderLayout.WEST);

        // 右侧用户信息
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(new Color(245, 250, 255));

        // 用户名
        JLabel userNameLabel = new JLabel(student.getRealName());
        userNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        userNameLabel.setForeground(new Color(34, 34, 34));
        userNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 角色标签
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

        // ID信息
        JLabel idLabel = new JLabel("ID: " + student.getUserId());
        idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        idLabel.setForeground(new Color(120, 120, 120));
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userInfoPanel.add(userNameLabel);
        userInfoPanel.add(Box.createVerticalStrut(8));
        userInfoPanel.add(rolePanel);
        userInfoPanel.add(Box.createVerticalStrut(5));
        userInfoPanel.add(idLabel);

        userPanel.add(userInfoPanel, BorderLayout.CENTER);
        // 个人信息高度占满
        Dimension pref = userPanel.getPreferredSize();
        userPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));

        sidebar.add(userPanel);

        // 分割线
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(180, 1));
        separator.setForeground(new Color(240, 240, 240));
        sidebar.add(separator);

        // 导航菜单
        String[][] menuConfig = {
            {"home", "我的主页"},
            {"exam", "考试题库"},
            {"score", "成绩查询"},
            {"achievement", "我的成就"}
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
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 0));
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        
        // 设置图标
        Icon icon = getMenuIcon(view, isActive ? UIUtil.PRIMARY_COLOR : new Color(120, 120, 120), 16);
        button.setIcon(icon);
        button.setIconTextGap(10);

        // 设置初始样式
        updateButtonStyle(button, isActive);

        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // 如果不是当前激活按钮，显示悬停效果
                if (!isButtonActive(button)) {
                    button.setBackground(new Color(248, 249, 250));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // 如果不是当前激活按钮，恢复默认样式
                if (!isButtonActive(button)) {
                    button.setBackground(Color.WHITE);
                }
            }
        });

        return button;
    }

    /**
     * 获取菜单图标
     */
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
            default:
                return IconUtil.createCircleIcon(color, size);
        }
    }
    
    /**
     * 检查按钮是否为激活状态
     */
    private boolean isButtonActive(JButton button) {
        // 通过背景颜色判断是否为激活状态
        return button.getBackground().equals(new Color(240, 248, 255));
    }

    /**
     * 更新按钮样式
     * @param button 按钮对象
     * @param isActive 是否为激活状态
     */
    private void updateButtonStyle(JButton button, boolean isActive) {
        if (isActive) {
            // 激活状态：浅蓝色背景 + 蓝色字体 + 左侧蓝色竖线
            button.setBackground(new Color(240, 248, 255));
            button.setForeground(UIUtil.PRIMARY_COLOR);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, UIUtil.PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(12, 17, 12, 10)
            ));
        } else {
            // 默认状态：白色背景 + 黑色字体
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(51, 51, 51));
            button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 0));
        }
    }
    
    /**
     * 更新按钮图标颜色
     */
    private void updateButtonIcon(JButton button, String view, boolean isActive) {
        Color iconColor = isActive ? UIUtil.PRIMARY_COLOR : new Color(120, 120, 120);
        Icon icon = getMenuIcon(view, iconColor, 16);
        button.setIcon(icon);
    }

    /**
     * 切换视图
     * @param view 视图名称（home/exam/score/practice等）
     */
    private void switchView(String view) {
        // 防止重复切换
        if (currentView.equals(view)) {
            return;
        }

        currentView = view;

        // 更新所有按钮的状态（只有当前视图对应的按钮为激活状态）
        String[] views = {"home", "exam", "score", "achievement"};
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
                mainContentPanel.add(createExamPanel(), BorderLayout.CENTER);
                break;
            case "score":
                mainContentPanel.add(createScorePanel(), BorderLayout.CENTER);
                break;
            case "achievement":
                mainContentPanel.add(createAchievementPanel(), BorderLayout.CENTER);
                break;
            default:
                mainContentPanel.add(createHomePanel(), BorderLayout.CENTER);
        }

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    /**
     * 根据视图名称获取对应的按钮索引
     */
    private int getViewIndex(String view) {
        switch (view) {
            case "home": return 0;
            case "exam": return 1;
            case "score": return 2;
            case "achievement": return 3;
            default: return -1;
        }
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);

        // 欢迎横幅 - 使用渐变蓝色背景
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

        // 主内容区 - 增加卡片尺寸
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
                // 点击卡片时跳转到对应视图
                if (targetView != null) {
                    switchView(targetView);
                }
            }
        });

        return card;
    }

    private JPanel createExamPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(Color.WHITE);

        // 左侧科目分类栏
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBackground(new Color(250, 250, 250));
        categoryPanel.setPreferredSize(new Dimension(180, 0));
        categoryPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        // 分类标题
        JPanel categoryTitlePanel = new JPanel(new BorderLayout());
        categoryTitlePanel.setBackground(new Color(250, 250, 250));
        categoryTitlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        categoryTitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JLabel categoryTitleLabel = new JLabel("科目分类");
        categoryTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        categoryTitleLabel.setForeground(UIUtil.TEXT_COLOR);
        categoryTitlePanel.add(categoryTitleLabel, BorderLayout.WEST);
        
        categoryPanel.add(categoryTitlePanel);
        
        // 分隔线
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setForeground(new Color(230, 230, 230));
        categoryPanel.add(separator);
        
        // 科目列表
        for (String subject : SUBJECTS) {
            JButton subjectButton = createSubjectButton(subject, subject.equals(currentSubject));
            subjectButton.addActionListener(e -> {
                currentSubject = subject;
                refreshSubjectButtons(categoryPanel);
                loadPapersBySubject(subject);
            });
            categoryPanel.add(subjectButton);
        }
        
        categoryPanel.add(Box.createVerticalGlue());
        
        panel.add(categoryPanel, BorderLayout.WEST);

        // 右侧主内容区
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 标题区域
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel titleLabel = new JLabel("考试列表");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(UIUtil.TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // 当前科目显示
        JLabel currentSubjectLabel = new JLabel("当前科目：" + currentSubject);
        currentSubjectLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        currentSubjectLabel.setForeground(new Color(100, 100, 100));
        titlePanel.add(currentSubjectLabel, BorderLayout.CENTER);
        
        // 刷新按钮放在标题区域右侧
        JButton refreshButton = new JButton("刷新列表");
        refreshButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> loadPapersBySubject(currentSubject));
        titlePanel.add(refreshButton, BorderLayout.EAST);

        contentPanel.add(titlePanel, BorderLayout.NORTH);

        // 考试记录表格区域
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
        
        // 试卷列表表格 - 根据图片设计
        String[] columns = {"名称", "单选", "多选", "判断", "填空", "操作"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // 只有操作列可编辑（用于按钮点击）
            }
        };
        paperTable = new JTable(tableModel);
        paperTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        paperTable.setRowHeight(45);
        paperTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paperTable.setGridColor(new Color(230, 230, 230));
        paperTable.setShowGrid(true);
        paperTable.setSelectionBackground(new Color(232, 240, 254));
        paperTable.setSelectionForeground(UIUtil.TEXT_COLOR);
        
        // 为操作列设置按钮渲染器和编辑器
        paperTable.getColumn("操作").setCellRenderer(new ButtonRenderer());
        paperTable.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // 表头样式
        paperTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        paperTable.getTableHeader().setBackground(new Color(245, 247, 250));
        paperTable.getTableHeader().setForeground(UIUtil.TEXT_COLOR);
        paperTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        paperTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        
        JScrollPane scrollPane = new JScrollPane(paperTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        // 初始化表格数据
        loadPapersBySubject(currentSubject);
        
        return panel;
    }

    private JPanel createScorePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 标题区域
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel titleLabel = new JLabel("成绩查询");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(UIUtil.TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        panel.add(titlePanel, BorderLayout.NORTH);

        // 成绩记录表格区域
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
        
        // 成绩表格
        String[] columns = {"试卷名称", "总分", "得分", "正确题数", "错误题数", "考试时间", "耗时", "详情"};
        DefaultTableModel scoreTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // 只有详情列可编辑
            }
        };
        JTable scoreTable = new JTable(scoreTableModel);
        scoreTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        scoreTable.setRowHeight(45);
        scoreTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scoreTable.setGridColor(new Color(230, 230, 230));
        scoreTable.setShowGrid(true);
        scoreTable.setSelectionBackground(new Color(232, 240, 254));
        scoreTable.setSelectionForeground(UIUtil.TEXT_COLOR);
        
        // 为详情列设置按钮渲染器和编辑器
        scoreTable.getColumn("详情").setCellRenderer(new ScoreDetailButtonRenderer());
        scoreTable.getColumn("详情").setCellEditor(new ScoreDetailButtonEditor(new JCheckBox(), scoreTableModel));
        
        // 表头样式
        scoreTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        scoreTable.getTableHeader().setBackground(new Color(245, 247, 250));
        scoreTable.getTableHeader().setForeground(UIUtil.TEXT_COLOR);
        scoreTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        scoreTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.CENTER);
        
        // 加载成绩数据
        loadScores(scoreTableModel);
        
        return panel;
    }
    
    private void loadScores(DefaultTableModel scoreTableModel) {
        scoreTableModel.setRowCount(0);
        try {
            List<ExamRecord> records = examService.getStudentExamRecords(student.getUserId());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            for (ExamRecord record : records) {
                // 计算考试时长
                String duration = "";
                if (record.getStartTime() != null && record.getSubmitTime() != null) {
                    Duration d = Duration.between(record.getStartTime(), record.getSubmitTime());
                    long minutes = d.toMinutes();
                    long seconds = d.getSeconds() % 60;
                    duration = String.format("%d分%d秒", minutes, seconds);
                }
                
                // 获取详细答题记录
                List<AnswerRecord> answerRecords = examService.getAnswerRecords(record.getRecordId());
                long correctCount = answerRecords.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect()).count();
                long wrongCount = answerRecords.stream().filter(a -> a.getIsCorrect() != null && !a.getIsCorrect()).count();
                
                Object[] row = {
                    record.getPaper() != null ? record.getPaper().getPaperName() : "未知",
                    record.getPaper() != null ? record.getPaper().getTotalScore() : 0,
                    record.getScore() != null ? record.getScore() : 0,
                    correctCount,
                    wrongCount,
                    record.getSubmitTime() != null ? record.getSubmitTime().format(formatter) : "",
                    duration,
                    "查看详情"
                };
                scoreTableModel.addRow(row);
            }
            
            if (records.isEmpty()) {
                // 显示提示信息
                Object[] row = {"暂无考试记录", "", "", "", "", "", "", ""};
                scoreTableModel.addRow(row);
            }
        } catch (Exception e) {
            UIUtil.showError(this, "加载成绩失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建科目按钮
     */
    private JButton createSubjectButton(String subject, boolean isActive) {
        JButton button = new JButton(subject);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        
        // 设置图标
        Icon icon = IconUtil.createCircleIcon(
            isActive ? UIUtil.PRIMARY_COLOR : new Color(150, 150, 150), 8);
        button.setIcon(icon);
        button.setIconTextGap(12);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
        
        // 设置样式
        updateSubjectButtonStyle(button, isActive);
        
        // 悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!button.getBackground().equals(new Color(232, 240, 254))) {
                    button.setBackground(new Color(245, 245, 245));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!button.getBackground().equals(new Color(232, 240, 254))) {
                    button.setBackground(new Color(250, 250, 250));
                }
            }
        });
        
        return button;
    }
    
    /**
     * 更新科目按钮样式
     */
    private void updateSubjectButtonStyle(JButton button, boolean isActive) {
        if (isActive) {
            button.setBackground(new Color(232, 240, 254));
            button.setForeground(UIUtil.PRIMARY_COLOR);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, UIUtil.PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(12, 17, 12, 10)
            ));
            Icon icon = IconUtil.createCircleIcon(UIUtil.PRIMARY_COLOR, 8);
            button.setIcon(icon);
        } else {
            button.setBackground(new Color(250, 250, 250));
            button.setForeground(new Color(80, 80, 80));
            button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
            Icon icon = IconUtil.createCircleIcon(new Color(150, 150, 150), 8);
            button.setIcon(icon);
        }
    }
    
    /**
     * 刷新科目按钮状态
     */
    private void refreshSubjectButtons(JPanel categoryPanel) {
        Component[] components = categoryPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                String buttonText = button.getText();
                boolean isActive = buttonText.equals(currentSubject);
                updateSubjectButtonStyle(button, isActive);
            }
        }
    }
    
    /**
     * 根据科目加载试卷
     */
    private void loadPapersBySubject(String subject) {
        if (tableModel == null) {
            return;
        }
        tableModel.setRowCount(0);
        try {
            List<Paper> allPapers = paperService.getAllPapers();
            List<Paper> filteredPapers;
            
            if ("全部".equals(subject)) {
                filteredPapers = allPapers;
            } else {
                filteredPapers = new java.util.ArrayList<>();
                for (Paper p : allPapers) {
                    if (subject.equals(p.getSubject())) {
                        filteredPapers.add(p);
                    }
                }
            }
            
            for (Paper p : filteredPapers) {
                // 统计各类型题目数量
                long singleCount = 0;
                long multipleCount = 0;
                long judgeCount = 0;
                long blankCount = 0;
                
                if (p.getQuestions() != null && !p.getQuestions().isEmpty()) {
                    singleCount = p.getQuestions().stream()
                        .filter(q -> q.getQuestionType() == com.exam.model.enums.QuestionType.SINGLE)
                        .count();
                    multipleCount = p.getQuestions().stream()
                        .filter(q -> q.getQuestionType() == com.exam.model.enums.QuestionType.MULTIPLE)
                        .count();
                    judgeCount = p.getQuestions().stream()
                        .filter(q -> q.getQuestionType() == com.exam.model.enums.QuestionType.JUDGE)
                        .count();
                    blankCount = p.getQuestions().stream()
                        .filter(q -> q.getQuestionType() == com.exam.model.enums.QuestionType.BLANK)
                        .count();
                }
                
                Object[] row = {
                    p.getPaperName(),
                    singleCount > 0 ? String.valueOf(singleCount) : "无",
                    multipleCount > 0 ? String.valueOf(multipleCount) : "无",
                    judgeCount > 0 ? String.valueOf(judgeCount) : "无",
                    blankCount > 0 ? String.valueOf(blankCount) : "无",
                    "开始考试"
                };
                tableModel.addRow(row);
            }
            
            if (filteredPapers.isEmpty()) {
                UIUtil.showInfo(this, "该科目暂无试卷");
            }
        } catch (Exception e) {
            UIUtil.showError(this, "加载试卷失败：" + e.getMessage());
        }
    }

    private void loadPapers() {
        loadPapersBySubject(currentSubject);
    }

    private void startExam() {
        startExam(paperTable.getSelectedRow());
    }
    
    private void startExam(int selectedRow) {
        if (selectedRow == -1) {
            UIUtil.showWarning(this, "请先选择要学习的试卷");
            return;
        }
        
        String paperName = (String) tableModel.getValueAt(selectedRow, 0);
        if (paperName == null || paperName.isEmpty()) {
            UIUtil.showWarning(this, "请选择有效的试卷");
            return;
        }
        
        if (!UIUtil.showConfirm(this, "确定要开始考试《" + paperName + "》吗？\n考试开始后将开始计时。")) {
            return;
        }
        
        try {
            // 通过试卷名称获取试卷
            Paper paper = paperService.getPaperByName(paperName);
            if (paper == null || paper.getQuestions().isEmpty()) {
                UIUtil.showError(this, "该试卷没有题目，无法考试");
                return;
            }
            
            // 打开考试界面
            new ExamFrame(student, paper, examService).setVisible(true);
            
        } catch (Exception e) {
            UIUtil.showError(this, "开始考试失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void logout() {
        if (UIUtil.showConfirm(this, "确定要退出登录吗？")) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
    
    // 创建成就面板
    private JPanel createAchievementPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 标题区域
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("我的成就");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        panel.add(titlePanel, BorderLayout.NORTH);

        // 主内容区域
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(Color.WHITE);

        // 统计卡片区域
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        try {
            // 只获取一次考试记录，避免重复查询
            List<ExamRecord> records = examService.getStudentExamRecords(student.getUserId());
            
            // 统计数据
            int totalExams = records.size();
            double avgScore = records.stream()
                .filter(r -> r.getScore() != null)
                .mapToDouble(r -> r.getScore().doubleValue())
                .average()
                .orElse(0.0);
            long totalCorrect = 0;
            long totalQuestions = 0;
            
            for (ExamRecord record : records) {
                List<AnswerRecord> answerRecords = examService.getAnswerRecords(record.getRecordId());
                totalCorrect += answerRecords.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect()).count();
                totalQuestions += answerRecords.size();
            }
            
            double accuracy = totalQuestions > 0 ? (totalCorrect * 100.0 / totalQuestions) : 0;

            // 创建统计卡片
            statsPanel.add(createStatCard(IconUtil.createDocumentIcon(new Color(52, 152, 219), 40), "考试次数", String.valueOf(totalExams), new Color(52, 152, 219)));
            statsPanel.add(createStatCard(IconUtil.createTargetIcon(new Color(46, 204, 113), 40), "平均分", String.format("%.1f", avgScore), new Color(46, 204, 113)));
            statsPanel.add(createStatCard(IconUtil.createCheckIcon(new Color(155, 89, 182), 40), "正确题数", String.valueOf(totalCorrect), new Color(155, 89, 182)));
            statsPanel.add(createStatCard(IconUtil.createTrendUpIcon(new Color(231, 76, 60), 40), "正确率", String.format("%.1f%%", accuracy), new Color(231, 76, 60)));

        } catch (Exception e) {
            e.printStackTrace();
        }

        contentPanel.add(statsPanel, BorderLayout.NORTH);

        // 图表区域（传递考试记录以避免重复查询）
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartsPanel.setBackground(Color.WHITE);

        // 成绩趋势图
        chartsPanel.add(createScoreTrendChart());
        
        // 题型准确率图
        chartsPanel.add(createAccuracyChart());

        contentPanel.add(chartsPanel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }
    
    // 创建统计卡片
    private JPanel createStatCard(Icon icon, String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        
        JLabel iconLabel = new JLabel(icon);
        topPanel.add(iconLabel, BorderLayout.WEST);

        card.add(topPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        labelLabel.setForeground(new Color(120, 120, 120));
        labelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottomPanel.add(valueLabel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(labelLabel);

        card.add(bottomPanel, BorderLayout.CENTER);

        return card;
    }
    
    // 创建成绩趋势图
    private JPanel createScoreTrendChart() {
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("成绩趋势图");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        chartPanel.add(titleLabel, BorderLayout.NORTH);

        // 绘制区域
        JPanel drawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int padding = 40;

                try {
                    List<ExamRecord> records = examService.getStudentExamRecords(student.getUserId());
                    if (records.isEmpty()) {
                        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        g2d.setColor(new Color(150, 150, 150));
                        String msg = "暂无考试记录";
                        FontMetrics fm = g2d.getFontMetrics();
                        int msgWidth = fm.stringWidth(msg);
                        g2d.drawString(msg, (width - msgWidth) / 2, height / 2);
                        return;
                    }

                    // 绘制坐标轴
                    g2d.setColor(new Color(200, 200, 200));
                    g2d.drawLine(padding, height - padding, width - padding, height - padding); // X轴
                    g2d.drawLine(padding, padding, padding, height - padding); // Y轴

                    // 绘制网格线
                    g2d.setColor(new Color(240, 240, 240));
                    for (int i = 1; i <= 4; i++) {
                        int y = padding + (height - 2 * padding) * i / 5;
                        g2d.drawLine(padding, y, width - padding, y);
                    }

                    if (records.size() > 0) {
                        int maxScore = 100;
                        int chartWidth = width - 2 * padding;
                        int chartHeight = height - 2 * padding;
                        int pointGap = chartWidth / Math.max(records.size() - 1, 1);

                        // 绘制数据点和线条
                        g2d.setStroke(new BasicStroke(2));
                        g2d.setColor(new Color(52, 152, 219));

                        int prevX = padding;
                        int firstScore = records.get(0).getScore() != null ? records.get(0).getScore().intValue() : 0;
                        int prevY = height - padding - (firstScore * chartHeight / maxScore);

                        for (int i = 0; i < records.size(); i++) {
                            ExamRecord record = records.get(i);
                            int x = padding + (records.size() > 1 ? i * pointGap : chartWidth / 2);
                            int score = record.getScore() != null ? record.getScore().intValue() : 0;
                            int y = height - padding - (score * chartHeight / maxScore);

                            if (i > 0) {
                                g2d.drawLine(prevX, prevY, x, y);
                            }

                            // 绘制数据点
                            g2d.fillOval(x - 4, y - 4, 8, 8);

                            // 显示分数
                            g2d.setFont(new Font("微软雅黑", Font.PLAIN, 11));
                            String scoreStr = String.valueOf(score);
                            FontMetrics fm = g2d.getFontMetrics();
                            g2d.drawString(scoreStr, x - fm.stringWidth(scoreStr) / 2, y - 10);

                            // 显示考试序号
                            g2d.setColor(new Color(120, 120, 120));
                            String label = "#" + (i + 1);
                            g2d.drawString(label, x - fm.stringWidth(label) / 2, height - padding + 20);
                            g2d.setColor(new Color(52, 152, 219));

                            prevX = x;
                            prevY = y;
                        }
                    }

                    // Y轴刻度
                    g2d.setColor(new Color(120, 120, 120));
                    g2d.setFont(new Font("微软雅黑", Font.PLAIN, 11));
                    for (int i = 0; i <= 5; i++) {
                        int score = i * 20;
                        int y = height - padding - (height - 2 * padding) * i / 5;
                        g2d.drawString(String.valueOf(score), padding - 30, y + 5);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        drawPanel.setBackground(Color.WHITE);
        chartPanel.add(drawPanel, BorderLayout.CENTER);

        return chartPanel;
    }
    
    // 创建题型准确率图（柱状图）
    private JPanel createAccuracyChart() {
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("题型准确率分析");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        chartPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel drawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int padding = 40;

                try {
                    // 统计各题型准确率
                    List<ExamRecord> records = examService.getStudentExamRecords(student.getUserId());
                    if (records.isEmpty()) {
                        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        g2d.setColor(new Color(150, 150, 150));
                        String msg = "暂无考试记录";
                        FontMetrics fm = g2d.getFontMetrics();
                        int msgWidth = fm.stringWidth(msg);
                        g2d.drawString(msg, (width - msgWidth) / 2, height / 2);
                        return;
                    }

                    int[] correctCounts = new int[4]; // 单选、多选、判断、填空
                    int[] totalCounts = new int[4];

                    for (ExamRecord record : records) {
                        List<AnswerRecord> answerRecords = examService.getAnswerRecords(record.getRecordId());
                        for (AnswerRecord ar : answerRecords) {
                            if (ar.getQuestion() != null) {
                                int index = -1;
                                switch (ar.getQuestion().getQuestionType()) {
                                    case SINGLE: index = 0; break;
                                    case MULTIPLE: index = 1; break;
                                    case JUDGE: index = 2; break;
                                    case BLANK: index = 3; break;
                                }
                                if (index >= 0) {
                                    totalCounts[index]++;
                                    if (ar.getIsCorrect() != null && ar.getIsCorrect()) {
                                        correctCounts[index]++;
                                    }
                                }
                            }
                        }
                    }

                    // 绘制坐标轴
                    g2d.setColor(new Color(200, 200, 200));
                    g2d.drawLine(padding, height - padding, width - padding, height - padding); // X轴
                    g2d.drawLine(padding, padding, padding, height - padding); // Y轴

                    // 绘制网格线
                    g2d.setColor(new Color(240, 240, 240));
                    for (int i = 1; i <= 4; i++) {
                        int y = padding + (height - 2 * padding) * i / 5;
                        g2d.drawLine(padding, y, width - padding, y);
                    }

                    // 绘制柱状图
                    String[] labels = {"单选题", "多选题", "判断题", "填空题"};
                    Color[] colors = {
                        new Color(52, 152, 219),
                        new Color(46, 204, 113),
                        new Color(155, 89, 182),
                        new Color(241, 196, 15)
                    };

                    int barWidth = (width - 2 * padding - 60) / 4;
                    int chartHeight = height - 2 * padding;

                    for (int i = 0; i < 4; i++) {
                        double accuracy = totalCounts[i] > 0 ? (correctCounts[i] * 100.0 / totalCounts[i]) : 0;
                        int barHeight = (int) (chartHeight * accuracy / 100);
                        int x = padding + 30 + i * (barWidth + 15);
                        int y = height - padding - barHeight;

                        // 绘制柱形
                        g2d.setColor(colors[i]);
                        g2d.fillRect(x, y, barWidth, barHeight);

                        // 绘制边框
                        g2d.setColor(colors[i].darker());
                        g2d.drawRect(x, y, barWidth, barHeight);

                        // 显示百分比
                        g2d.setFont(new Font("微软雅黑", Font.BOLD, 12));
                        String percentStr = String.format("%.1f%%", accuracy);
                        FontMetrics fm = g2d.getFontMetrics();
                        g2d.setColor(new Color(60, 60, 60));
                        g2d.drawString(percentStr, x + (barWidth - fm.stringWidth(percentStr)) / 2, y - 5);

                        // 显示标签
                        g2d.setColor(new Color(120, 120, 120));
                        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 11));
                        g2d.drawString(labels[i], x + (barWidth - fm.stringWidth(labels[i])) / 2, height - padding + 20);
                    }

                    // Y轴刻度
                    g2d.setColor(new Color(120, 120, 120));
                    g2d.setFont(new Font("微软雅黑", Font.PLAIN, 11));
                    for (int i = 0; i <= 5; i++) {
                        int percent = i * 20;
                        int y = height - padding - chartHeight * i / 5;
                        g2d.drawString(percent + "%", padding - 35, y + 5);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        drawPanel.setBackground(Color.WHITE);
        chartPanel.add(drawPanel, BorderLayout.CENTER);

        return chartPanel;
    }
    
    // 显示考试详情对话框
    private void showExamDetail(int recordId) {
        try {
            ExamRecord record = examService.getExamRecordById(recordId);
            if (record == null) {
                UIUtil.showError(this, "找不到考试记录");
                return;
            }
            
            List<AnswerRecord> answerRecords = examService.getAnswerRecords(recordId);
            
            // 创建对话框
            JDialog dialog = new JDialog(this, "考试详情", true);
            dialog.setSize(900, 600);
            dialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // 顶部信息
            JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 10));
            infoPanel.add(new JLabel("试卷名称：" + (record.getPaper() != null ? record.getPaper().getPaperName() : "未知")));
            infoPanel.add(new JLabel("总分：" + (record.getPaper() != null ? record.getPaper().getTotalScore() : 0) + " 分"));
            infoPanel.add(new JLabel("得分：" + (record.getScore() != null ? record.getScore() : 0) + " 分"));
            infoPanel.add(new JLabel("考试时间：" + (record.getSubmitTime() != null ? record.getSubmitTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "")));
            
            long correctCount = answerRecords.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect()).count();
            long wrongCount = answerRecords.stream().filter(a -> a.getIsCorrect() != null && !a.getIsCorrect()).count();
            infoPanel.add(new JLabel("正确题数：" + correctCount));
            infoPanel.add(new JLabel("错误题数：" + wrongCount));
            
            panel.add(infoPanel, BorderLayout.NORTH);
            
            // 错题详情表格
            String[] columns = {"题号", "题目类型", "题目内容", "正确答案", "你的答案", "是否正确"};
            DefaultTableModel detailModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            JTable detailTable = new JTable(detailModel);
            detailTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            detailTable.setRowHeight(35);
            detailTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
            
            // 填充错题数据
            int questionNo = 1;
            for (AnswerRecord ar : answerRecords) {
                if (ar.getQuestion() != null) {
                    String isCorrect = ar.getIsCorrect() != null ? (ar.getIsCorrect() ? "✓ 正确" : "✗ 错误") : "未答";
                    String content = ar.getQuestion().getContent();
                    if (content.length() > 30) {
                        content = content.substring(0, 30) + "...";
                    }
                    
                    Object[] row = {
                        questionNo++,
                        ar.getQuestion().getQuestionType() != null ? ar.getQuestion().getQuestionType().getDescription() : "",
                        content,
                        ar.getQuestion().getCorrectAnswer(),
                        ar.getStudentAnswer() != null ? ar.getStudentAnswer() : "未答",
                        isCorrect
                    };
                    detailModel.addRow(row);
                }
            }
            
            JScrollPane scrollPane = new JScrollPane(detailTable);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // 关闭按钮
            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(e -> dialog.dispose());
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.add(panel);
            dialog.setVisible(true);
            
        } catch (Exception e) {
            UIUtil.showError(this, "加载详情失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 表格按钮渲染器
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            setFont(new Font("微软雅黑", Font.PLAIN, 12));
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
            setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            setFocusPainted(false);
            return this;
        }
    }
    
    // 表格按钮编辑器
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            button.setFocusPainted(false);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                startExam(currentRow);
            }
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
    
    // 成绩详情按钮渲染器
    class ScoreDetailButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ScoreDetailButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            setFont(new Font("微软雅黑", Font.PLAIN, 12));
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
            setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            setFocusPainted(false);
            return this;
        }
    }
    
    // 成绩详情按钮编辑器
    class ScoreDetailButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;
        private DefaultTableModel tableModel;
        
        public ScoreDetailButtonEditor(JCheckBox checkBox, DefaultTableModel tableModel) {
            super(checkBox);
            this.tableModel = tableModel;
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            button.setFocusPainted(false);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // 获取recordId，需要修改loadScores方法来存储recordId
                try {
                    List<ExamRecord> records = examService.getStudentExamRecords(student.getUserId());
                    if (currentRow < records.size()) {
                        showExamDetail(records.get(currentRow).getRecordId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}
