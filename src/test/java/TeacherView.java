//import com.exam.model.Question;
//import com.exam.model.User;
//import com.exam.model.Paper;
//import com.exam.model.enums.QuestionType;
//import com.exam.model.enums.Difficulty;
//import com.exam.dao.PaperDao;
//import com.exam.service.QuestionService;
//import com.exam.service.PaperService;
//import com.exam.util.UIUtil;
//import com.exam.util.IconUtil;
//import com.exam.util.QuestionImportUtil;
//import com.exam.view.LoginFrame;
//import com.exam.view.teacher.TeacherMainFrame;
//
//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import javax.swing.filechooser.FileNameExtensionFilter;
//import java.awt.*;
//import java.util.List;
//import java.util.ArrayList;
//import java.io.File;
//
///**
// * 教师主界面
// */
//public class TeacherView extends JFrame {
//    private final User teacher;
//    private final QuestionService questionService;
//    private final PaperService paperService;
//    private final PaperDao paperDao;
//    private JTable questionTable;
//    private DefaultTableModel tableModel;
//    private JPanel mainContentPanel;
//    private String currentView = "home";
//    private List<JButton> menuButtons = new ArrayList<>();
//    private String currentSubject = "全部"; // 当前选中的科目
//    private static final String[] SUBJECTS = {"全部", "Java", "Vue", "数据结构", "马克思主义", "计算机网络", "操作系统", "数据库"};
//
//    public TeacherView(User teacher) {
//        this.teacher = teacher;
//        this.questionService = new QuestionService();
//        this.paperService = new PaperService();
//        this.paperDao = new PaperDao();
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
//        // 顶部面板 - 与学生界面一致
//        JPanel topPanel = new JPanel(new BorderLayout());
//        topPanel.setBackground(Color.WHITE);
//        topPanel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
//            BorderFactory.createEmptyBorder(15, 30, 15, 30)
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
//            BorderFactory.createLineBorder(new Color(220, 220, 220)),
//            BorderFactory.createEmptyBorder(5, 15, 5, 15)
//        ));
//        logoutButton.setFocusPainted(false);
//        logoutButton.addActionListener(e -> logout());
//        rightPanel.add(logoutButton);
//
//        topPanel.add(rightPanel, BorderLayout.EAST);
//
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
//        mainContentPanel.add(createHomePanel(), BorderLayout.CENTER); // 默认显示主页
//
//        contentPanel.add(mainContentPanel, BorderLayout.CENTER);
//
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
//            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 230, 240)),
//            BorderFactory.createEmptyBorder(30, 0, 30, 20)
//        ));
//
//        // 左侧头像区域
//        JPanel avatarPanel = new JPanel();
//        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
//        avatarPanel.setBackground(new Color(245, 250, 255));
//
//        // 头像圆形背景
//        JPanel avatarCircle = new JPanel(new GridBagLayout());
//        avatarCircle.setPreferredSize(new Dimension(60, 60));
//        avatarCircle.setMaximumSize(new Dimension(60, 60));
//        avatarCircle.setBackground(UIUtil.PRIMARY_COLOR);
//        avatarCircle.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 240), 2));
//
//        JLabel userIconLabel = new JLabel("👨‍🏫");
//        userIconLabel.setFont(new Font("微软雅黑", Font.PLAIN, 28));
//        avatarCircle.add(userIconLabel);
//
//        avatarPanel.add(avatarCircle);
//        userPanel.add(avatarPanel, BorderLayout.WEST);
//
//        // 右侧用户信息
//        JPanel userInfoPanel = new JPanel();
//        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
//        userInfoPanel.setBackground(new Color(245, 250, 255));
//
//        // 用户名
//        JLabel userNameLabel = new JLabel(teacher.getRealName());
//        userNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
//        userNameLabel.setForeground(new Color(34, 34, 34));
//        userNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        // 角色标签
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
//        // ID信息
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
//            {"home", "我的主页"},
//            {"question", "题库管理"},
//            {"paper", "试卷管理"},
//            {"import", "导入题目"}
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
//        // 设置图标
//        Icon icon = getMenuIcon(view, isActive ? UIUtil.PRIMARY_COLOR : new Color(120, 120, 120), 16);
//        button.setIcon(icon);
//        button.setIconTextGap(10);
//
//        // 设置初始样式
//        updateButtonStyle(button, isActive);
//
//        // 添加鼠标悬停效果
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
//                BorderFactory.createMatteBorder(0, 3, 0, 0, UIUtil.PRIMARY_COLOR),
//                BorderFactory.createEmptyBorder(12, 17, 12, 10)
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
//                mainContentPanel.add(createQuestionPanel(), BorderLayout.CENTER);
//                break;
//            case "paper":
//                mainContentPanel.add(createPaperPanel(), BorderLayout.CENTER);
//                break;
//            case "import":
//                mainContentPanel.add(createImportPanel(), BorderLayout.CENTER);
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
//            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 220, 240)),
//            BorderFactory.createEmptyBorder(40, 40, 40, 40)
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
//
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
//            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
//            BorderFactory.createEmptyBorder(30, 25, 30, 25)
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
//                    BorderFactory.createLineBorder(color, 2),
//                    BorderFactory.createEmptyBorder(30, 25, 30, 25)
//                ));
//            }
//            public void mouseExited(java.awt.event.MouseEvent evt) {
//                card.setBackground(Color.WHITE);
//                card.setBorder(BorderFactory.createCompoundBorder(
//                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
//                    BorderFactory.createEmptyBorder(30, 25, 30, 25)
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
//    private JPanel createQuestionPanel() {
//        JPanel panel = new JPanel(new BorderLayout(0, 0));
//        panel.setBackground(Color.WHITE);
//
//        // 左侧科目分类栏
//        JPanel categoryPanel = new JPanel();
//        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
//        categoryPanel.setBackground(new Color(250, 250, 250));
//        categoryPanel.setPreferredSize(new Dimension(180, 0));
//        categoryPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));
//
//        // 分类标题
//        JPanel categoryTitlePanel = new JPanel(new BorderLayout());
//        categoryTitlePanel.setBackground(new Color(250, 250, 250));
//        categoryTitlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
//        categoryTitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
//
//        JLabel categoryTitleLabel = new JLabel("科目分类");
//        categoryTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
//        categoryTitleLabel.setForeground(UIUtil.TEXT_COLOR);
//        categoryTitlePanel.add(categoryTitleLabel, BorderLayout.WEST);
//
//        categoryPanel.add(categoryTitlePanel);
//
//        // 分隔线
//        JSeparator separator = new JSeparator();
//        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
//        separator.setForeground(new Color(230, 230, 230));
//        categoryPanel.add(separator);
//
//        // 科目列表
//        for (String subject : SUBJECTS) {
//            JButton subjectButton = createSubjectButton(subject, subject.equals(currentSubject));
//            subjectButton.addActionListener(e -> {
//                currentSubject = subject;
//                refreshSubjectButtons(categoryPanel);
//                loadQuestionsBySubject(subject);
//            });
//            categoryPanel.add(subjectButton);
//        }
//
//        categoryPanel.add(Box.createVerticalGlue());
//
//        panel.add(categoryPanel, BorderLayout.WEST);
//
//        // 右侧主内容区
//        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
//        contentPanel.setBackground(Color.WHITE);
//        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
//
//        // 标题区域
//        JPanel titlePanel = new JPanel(new BorderLayout());
//        titlePanel.setBackground(Color.WHITE);
//        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
//
//        JLabel titleLabel = new JLabel("题库管理");
//        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
//        titleLabel.setForeground(UIUtil.TEXT_COLOR);
//        titlePanel.add(titleLabel, BorderLayout.WEST);
//
//        // 添加题目按钮放在右侧
//        JButton addButton = createStyledButton("添加题目", UIUtil.SUCCESS_COLOR);
//        addButton.addActionListener(e -> showAddQuestionDialog());
//        titlePanel.add(addButton, BorderLayout.EAST);
//
//        contentPanel.add(titlePanel, BorderLayout.NORTH);
//
//        // 表格面板
//        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
//        tablePanel.setBackground(Color.WHITE);
//        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
//
//        // 表格
//        String[] columns = {"科目", "类型", "题目内容", "正确答案", "操作"};
//        tableModel = new DefaultTableModel(columns, 0) {
//            @Override
//            public boolean isCellEditable(int row, int column) {
//                // 操作列可编辑
//                return column == 4;
//            }
//        };
//        questionTable = new JTable(tableModel) {
//            @Override
//            public Class<?> getColumnClass(int column) {
//                // 操作列使用JPanel类型
//                if (column == 4) {
//                    return JPanel.class;
//                }
//                return String.class;
//            }
//        };
//        questionTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
//        questionTable.setRowHeight(50);
//        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        questionTable.setGridColor(new Color(230, 230, 230));
//        questionTable.setShowGrid(true);
//        questionTable.setSelectionBackground(new Color(232, 240, 254));
//        questionTable.setSelectionForeground(UIUtil.TEXT_COLOR);
//
//        // 设置操作列渲染器
//        questionTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonPanelRenderer());
//        questionTable.getColumnModel().getColumn(4).setCellEditor(new ButtonPanelEditor(questionTable));
//
//        // 表头样式
//        questionTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
//        questionTable.getTableHeader().setBackground(new Color(245, 247, 250));
//        questionTable.getTableHeader().setForeground(UIUtil.TEXT_COLOR);
//        questionTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
//        questionTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
//
//        JScrollPane scrollPane = new JScrollPane(questionTable);
//        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
//        scrollPane.getViewport().setBackground(Color.WHITE);
//
//        tablePanel.add(scrollPane, BorderLayout.CENTER);
//        contentPanel.add(tablePanel, BorderLayout.CENTER);
//
//        panel.add(contentPanel, BorderLayout.CENTER);
//
//        // 加载数据
//        loadQuestionsBySubject(currentSubject);
//
//        return panel;
//    }
//
//    private JTable paperManagementTable;
//    private DefaultTableModel paperTableModel;
//
//    // ========================================
//    // 试卷管理相关方法（前置声明）
//    // ========================================
//
//    /**
//     * 加载试卷数据
//     */
//    private void loadPapersData() {
//        paperTableModel.setRowCount(0);
//        try {
//            List<Paper> papers = paperService.getAllPapers();
//            for (Paper paper : papers) {
//                int questionCount = paper.getQuestions() != null ? paper.getQuestions().size() : 0;
//                Object[] row = {
//                    paper.getPaperName(),
//                    paper.getSubject(),
//                    questionCount,
//                    paper.getTotalScore(),
//                    paper.getDuration(),
//                    paper.getPassScore(),
//                    "" // 操作列，由渲染器处理
//                };
//                paperTableModel.addRow(row);
//            }
//        } catch (Exception e) {
//            UIUtil.showError(this, "加载试卷失败：" + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 显示添加试卷对话框
//     */
//    private void showAddPaperDialog() {
//        JDialog dialog = new JDialog(this, "创建试卷", true);
//        dialog.setSize(700, 600);
//        dialog.setLocationRelativeTo(this);
//
//        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
//        mainPanel.setBackground(new Color(245, 250, 255));
//        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
//
//        // 标题
//        JLabel titleLabel = new JLabel("创建新试卷");
//        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
//        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
//        mainPanel.add(titleLabel, BorderLayout.NORTH);
//
//        // 表单
//        JPanel formPanel = new JPanel(new GridBagLayout());
//        formPanel.setBackground(Color.WHITE);
//        formPanel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createLineBorder(new Color(220, 220, 220)),
//            BorderFactory.createEmptyBorder(20, 20, 20, 20)
//        ));
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(8, 8, 8, 8);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//
//        // 试卷名称
//        gbc.gridx = 0; gbc.gridy = 0;
//        gbc.weightx = 0;
//        JLabel nameLabel = new JLabel("试卷名称：");
//        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(nameLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        JTextField paperNameField = new JTextField(20);
//        paperNameField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(paperNameField, gbc);
//
//        // 科目
//        gbc.gridx = 0; gbc.gridy = 1;
//        gbc.weightx = 0;
//        JLabel subjectLabel = new JLabel("科　　目：");
//        subjectLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(subjectLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        String[] subjectOptions = new String[SUBJECTS.length - 1];
//        System.arraycopy(SUBJECTS, 1, subjectOptions, 0, SUBJECTS.length - 1);
//        JComboBox<String> subjectCombo = new JComboBox<>(subjectOptions);
//        subjectCombo.setEditable(true);
//        subjectCombo.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(subjectCombo, gbc);
//
//        // 考试时长
//        gbc.gridx = 0; gbc.gridy = 2;
//        gbc.weightx = 0;
//        JLabel durationLabel = new JLabel("时长(分钟)：");
//        durationLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(durationLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(90, 10, 300, 10));
//        durationSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(durationSpinner, gbc);
//
//        // 及格分数
//        gbc.gridx = 0; gbc.gridy = 3;
//        gbc.weightx = 0;
//        JLabel passScoreLabel = new JLabel("及格分数：");
//        passScoreLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(passScoreLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        JSpinner passScoreSpinner = new JSpinner(new SpinnerNumberModel(60, 0, 100, 5));
//        passScoreSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(passScoreSpinner, gbc);
//
//        // 描述
//        gbc.gridx = 0; gbc.gridy = 4;
//        gbc.weightx = 0;
//        JLabel descLabel = new JLabel("描　　述：");
//        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(descLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        JTextArea descArea = new JTextArea(3, 20);
//        descArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        descArea.setLineWrap(true);
//        descArea.setWrapStyleWord(true);
//        JScrollPane descScroll = new JScrollPane(descArea);
//        formPanel.add(descScroll, gbc);
//
//        // 选择题目
//        gbc.gridx = 0; gbc.gridy = 5;
//        gbc.weightx = 0;
//        JLabel questionsLabel = new JLabel("选择题目：");
//        questionsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(questionsLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        JButton selectQuestionsButton = new JButton("选择题目");
//        selectQuestionsButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
//        selectQuestionsButton.setBackground(UIUtil.PRIMARY_COLOR);
//        selectQuestionsButton.setForeground(Color.BLACK);
//        selectQuestionsButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
//        selectQuestionsButton.setFocusPainted(false);
//
//        // 用于存储选中的题目ID
//        List<Integer> selectedQuestionIds = new ArrayList<>();
//        JLabel selectedCountLabel = new JLabel("已选 0 道题目");
//        selectedCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
//        selectedCountLabel.setForeground(new Color(100, 100, 100));
//
//        selectQuestionsButton.addActionListener(e -> {
//            showQuestionSelectionDialog(dialog, selectedQuestionIds, selectedCountLabel);
//        });
//
//        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
//        selectPanel.setBackground(Color.WHITE);
//        selectPanel.add(selectQuestionsButton);
//        selectPanel.add(selectedCountLabel);
//        formPanel.add(selectPanel, gbc);
//
//        mainPanel.add(formPanel, BorderLayout.CENTER);
//
//        // 按钮
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
//        buttonPanel.setBackground(new Color(245, 250, 255));
//
//        JButton confirmButton = createStyledButton("创建试卷", UIUtil.PRIMARY_COLOR);
//        confirmButton.addActionListener(e -> {
//            String paperName = paperNameField.getText().trim();
//            String subject = subjectCombo.getSelectedItem() != null
//                ? subjectCombo.getSelectedItem().toString().trim()
//                : "";
//
//            if (paperName.isEmpty()) {
//                UIUtil.showWarning(dialog, "试卷名称不能为空");
//                return;
//            }
//            if (subject.isEmpty()) {
//                UIUtil.showWarning(dialog, "科目不能为空");
//                return;
//            }
//            if (selectedQuestionIds.isEmpty()) {
//                UIUtil.showWarning(dialog, "请至少选择一道题目");
//                return;
//            }
//
//            try {
//                Paper paper = new Paper();
//                paper.setPaperName(paperName);
//                paper.setSubject(subject);
//                paper.setDuration((Integer) durationSpinner.getValue());
//                paper.setPassScore((Integer) passScoreSpinner.getValue());
//                paper.setDescription(descArea.getText().trim());
//                paper.setCreatorId(teacher.getUserId());
//
//                int paperId = paperService.createPaper(paper, selectedQuestionIds);
//
//                UIUtil.showInfo(dialog, "试卷创建成功！\n题目数：" + selectedQuestionIds.size() + " 道");
//                dialog.dispose();
//                loadPapersData();
//
//            } catch (Exception ex) {
//                UIUtil.showError(dialog, "创建试卷失败：" + ex.getMessage());
//                ex.printStackTrace();
//            }
//        });
//
//        JButton cancelButton = createStyledButton("取消", new Color(120, 144, 156));
//        cancelButton.addActionListener(e -> dialog.dispose());
//
//        buttonPanel.add(confirmButton);
//        buttonPanel.add(cancelButton);
//
//        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
//
//        dialog.add(mainPanel);
//        dialog.setVisible(true);
//    }
//
//    /**
//     * 显示题目选择对话框
//     */
//    private void showQuestionSelectionDialog(JDialog parentDialog, List<Integer> selectedQuestionIds, JLabel selectedCountLabel) {
//        JDialog dialog = new JDialog(parentDialog, "选择题目", true);
//        dialog.setSize(900, 600);
//        dialog.setLocationRelativeTo(parentDialog);
//
//        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
//        mainPanel.setBackground(Color.WHITE);
//        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//
//        // 标题
//        JLabel titleLabel = new JLabel("选择试卷题目");
//        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
//        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
//        mainPanel.add(titleLabel, BorderLayout.NORTH);
//
//        // 题目列表
//        String[] columns = {"选择", "题目ID", "科目", "类型", "题目内容", "分值"};
//        DefaultTableModel questionTableModel = new DefaultTableModel(columns, 0) {
//            @Override
//            public Class<?> getColumnClass(int column) {
//                return column == 0 ? Boolean.class : String.class;
//            }
//
//            @Override
//            public boolean isCellEditable(int row, int column) {
//                return column == 0; // 只有选择列可编辑
//            }
//        };
//
//        JTable questionSelectTable = new JTable(questionTableModel);
//        questionSelectTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        questionSelectTable.setRowHeight(40);
//        questionSelectTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//        questionSelectTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
//        questionSelectTable.getTableHeader().setBackground(new Color(245, 247, 250));
//
//        // 加载所有题目
//        try {
//            List<Question> allQuestions = questionService.getAllQuestions();
//            for (Question q : allQuestions) {
//                boolean isSelected = selectedQuestionIds.contains(q.getQuestionId());
//                Object[] row = {
//                    isSelected,
//                    q.getQuestionId(),
//                    q.getSubject(),
//                    q.getQuestionType().getDescription(),
//                    truncate(q.getContent(), 40),
//                    q.getScore()
//                };
//                questionTableModel.addRow(row);
//            }
//        } catch (Exception e) {
//            UIUtil.showError(dialog, "加载题目失败：" + e.getMessage());
//        }
//
//        JScrollPane scrollPane = new JScrollPane(questionSelectTable);
//        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
//        mainPanel.add(scrollPane, BorderLayout.CENTER);
//
//        // 按钮面板
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
//        buttonPanel.setBackground(Color.WHITE);
//
//        JButton confirmButton = createStyledButton("确定", UIUtil.PRIMARY_COLOR);
//        confirmButton.addActionListener(e -> {
//            selectedQuestionIds.clear();
//            for (int i = 0; i < questionTableModel.getRowCount(); i++) {
//                Boolean isSelected = (Boolean) questionTableModel.getValueAt(i, 0);
//                if (isSelected != null && isSelected) {
//                    String questionIdStr = questionTableModel.getValueAt(i, 1).toString();
//                    selectedQuestionIds.add(Integer.parseInt(questionIdStr));
//                }
//            }
//            selectedCountLabel.setText("已选 " + selectedQuestionIds.size() + " 道题目");
//            dialog.dispose();
//        });
//
//        JButton cancelButton = createStyledButton("取消", new Color(120, 144, 156));
//        cancelButton.addActionListener(e -> dialog.dispose());
//
//        buttonPanel.add(confirmButton);
//        buttonPanel.add(cancelButton);
//
//        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
//
//        dialog.add(mainPanel);
//        dialog.setVisible(true);
//    }
//
//    private JPanel createPaperPanel() {
//        JPanel panel = new JPanel(new BorderLayout(0, 0));
//        panel.setBackground(Color.WHITE);
//
//        // 右侧主内容区
//        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
//        contentPanel.setBackground(Color.WHITE);
//        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
//
//        // 标题区域
//        JPanel titlePanel = new JPanel(new BorderLayout());
//        titlePanel.setBackground(Color.WHITE);
//        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
//
//        JLabel titleLabel = new JLabel("试卷管理");
//        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
//        titleLabel.setForeground(UIUtil.TEXT_COLOR);
//        titlePanel.add(titleLabel, BorderLayout.WEST);
//
//        // 添加试卷按钮放在右侧
//        JButton addPaperButton = createStyledButton("创建试卷", UIUtil.SUCCESS_COLOR);
//        addPaperButton.addActionListener(e -> showAddPaperDialog());
//        titlePanel.add(addPaperButton, BorderLayout.EAST);
//
//        contentPanel.add(titlePanel, BorderLayout.NORTH);
//
//        // 表格面板
//        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
//        tablePanel.setBackground(Color.WHITE);
//        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
//
//        // 表格
//        String[] columns = {"试卷名称", "科目", "题目数", "总分", "时长(分钟)", "及格分", "操作"};
//        paperTableModel = new DefaultTableModel(columns, 0) {
//            @Override
//            public boolean isCellEditable(int row, int column) {
//                // 操作列可编辑
//                return column == 6;
//            }
//        };
//        paperManagementTable = new JTable(paperTableModel) {
//            @Override
//            public Class<?> getColumnClass(int column) {
//                // 操作列使用JPanel类型
//                if (column == 6) {
//                    return JPanel.class;
//                }
//                return String.class;
//            }
//        };
//        paperManagementTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
//        paperManagementTable.setRowHeight(50);
//        paperManagementTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        paperManagementTable.setGridColor(new Color(230, 230, 230));
//        paperManagementTable.setShowGrid(true);
//        paperManagementTable.setSelectionBackground(new Color(232, 240, 254));
//        paperManagementTable.setSelectionForeground(UIUtil.TEXT_COLOR);
//
//        // 设置操作列渲染器
//        paperManagementTable.getColumnModel().getColumn(6).setCellRenderer(new PaperButtonPanelRenderer());
//        paperManagementTable.getColumnModel().getColumn(6).setCellEditor(new PaperButtonPanelEditor(paperManagementTable));
//        // 设置操作列宽度
//        paperManagementTable.getColumnModel().getColumn(6).setPreferredWidth(180);
//        paperManagementTable.getColumnModel().getColumn(6).setMinWidth(180);
//
//        // 表头样式
//        paperManagementTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
//        paperManagementTable.getTableHeader().setBackground(new Color(245, 247, 250));
//        paperManagementTable.getTableHeader().setForeground(UIUtil.TEXT_COLOR);
//        paperManagementTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
//        paperManagementTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
//
//        JScrollPane scrollPane = new JScrollPane(paperManagementTable);
//        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
//        scrollPane.getViewport().setBackground(Color.WHITE);
//
//        tablePanel.add(scrollPane, BorderLayout.CENTER);
//        contentPanel.add(tablePanel, BorderLayout.CENTER);
//
//        panel.add(contentPanel, BorderLayout.CENTER);
//
//        // 加载数据
//        loadPapersData();
//
//        return panel;
//    }
//
//    /**
//     * 创建导入题目面板
//     */
//    private JPanel createImportPanel() {
//        JPanel panel = new JPanel(new BorderLayout(0, 0));
//        panel.setBackground(Color.WHITE);
//
//        // 主内容区
//        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
//        contentPanel.setBackground(Color.WHITE);
//        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
//
//        // 标题区域
//        JPanel titlePanel = new JPanel(new BorderLayout());
//        titlePanel.setBackground(Color.WHITE);
//
//        JLabel titleLabel = new JLabel("导入题目");
//        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
//        titleLabel.setForeground(UIUtil.TEXT_COLOR);
//        titlePanel.add(titleLabel, BorderLayout.WEST);
//
//        contentPanel.add(titlePanel, BorderLayout.NORTH);
//
//        // 中心内容区
//        JPanel centerPanel = new JPanel(new GridBagLayout());
//        centerPanel.setBackground(Color.WHITE);
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(10, 10, 10, 10);
//        gbc.fill = GridBagConstraints.BOTH;
//
//        // 创建三个主要区域
//        // 1. 文件选择区域
//        JPanel fileSelectionPanel = createFileSelectionPanel();
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        gbc.weightx = 1.0;
//        gbc.weighty = 0.25;
//        centerPanel.add(fileSelectionPanel, gbc);
//
//        // 2. 格式说明区域
//        JPanel formatPanel = createFormatDescriptionPanel();
//        gbc.gridy = 1;
//        gbc.weighty = 0.6;
//        centerPanel.add(formatPanel, gbc);
//
//        // 3. 操作按钮区域
//        JPanel actionPanel = createImportActionPanel();
//        gbc.gridy = 2;
//        gbc.weighty = 0.15;
//        centerPanel.add(actionPanel, gbc);
//
//        contentPanel.add(centerPanel, BorderLayout.CENTER);
//        panel.add(contentPanel, BorderLayout.CENTER);
//
//        return panel;
//    }
//
//    // 存储选择的文件
//    private File selectedImportFile = null;
//
//    /**
//     * 创建文件选择面板
//     */
//    private JPanel createFileSelectionPanel() {
//        JPanel panel = new JPanel(new BorderLayout(0, 10));
//        panel.setBackground(Color.WHITE);
//        panel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createTitledBorder(
//                BorderFactory.createLineBorder(new Color(200, 220, 240), 2),
//                "选择导入文件",
//                javax.swing.border.TitledBorder.LEFT,
//                javax.swing.border.TitledBorder.TOP,
//                new Font("微软雅黑", Font.BOLD, 14),
//                UIUtil.PRIMARY_COLOR
//            ),
//            BorderFactory.createEmptyBorder(10, 20, 15, 20)
//        ));
//
//        // 所有内容放在一行：文件图标 + 文件信息 + 按钮
//        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
//        contentPanel.setBackground(Color.WHITE);
//
//        // 文件图标
//        JLabel fileIconLabel = new JLabel("📄");
//        fileIconLabel.setFont(new Font("微软雅黑", Font.PLAIN, 32));
//        contentPanel.add(fileIconLabel);
//
//        // 文件信息面板（包含关闭按钮）
//        JPanel fileInfoWrapper = new JPanel(new BorderLayout(5, 0));
//        fileInfoWrapper.setBackground(Color.WHITE);
//
//        JPanel fileDetailsPanel = new JPanel();
//        fileDetailsPanel.setLayout(new BoxLayout(fileDetailsPanel, BoxLayout.Y_AXIS));
//        fileDetailsPanel.setBackground(Color.WHITE);
//
//        JLabel fileNameLabel = new JLabel("未选择文件");
//        fileNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
//        fileNameLabel.setForeground(new Color(100, 100, 100));
//        fileNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        JLabel filePathLabel = new JLabel("请点击右侧按钮选择题目文件（.txt格式）");
//        filePathLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
//        filePathLabel.setForeground(new Color(120, 120, 120));
//        filePathLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        fileDetailsPanel.add(fileNameLabel);
//        fileDetailsPanel.add(Box.createVerticalStrut(3));
//        fileDetailsPanel.add(filePathLabel);
//
//        fileInfoWrapper.add(fileDetailsPanel, BorderLayout.CENTER);
//
//        // 关闭按钮（右上角的×）
//        JButton closeButton = new JButton("×");
//        closeButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
//        closeButton.setForeground(new Color(150, 150, 150));
//        closeButton.setBackground(Color.WHITE);
//        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
//        closeButton.setFocusPainted(false);
//        closeButton.setContentAreaFilled(false);
//        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//        closeButton.setVisible(false); // 初始隐藏
//        closeButton.setPreferredSize(new Dimension(25, 25));
//
//        // 鼠标悬停效果
//        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mouseEntered(java.awt.event.MouseEvent evt) {
//                closeButton.setForeground(UIUtil.DANGER_COLOR);
//            }
//            public void mouseExited(java.awt.event.MouseEvent evt) {
//                closeButton.setForeground(new Color(150, 150, 150));
//            }
//        });
//
//        // 清除文件选择
//        closeButton.addActionListener(e -> {
//            selectedImportFile = null;
//            fileNameLabel.setText("未选择文件");
//            fileNameLabel.setForeground(new Color(100, 100, 100));
//            filePathLabel.setText("请点击右侧按钮选择题目文件（.txt格式）");
//            closeButton.setVisible(false);
//        });
//
//        fileInfoWrapper.add(closeButton, BorderLayout.EAST);
//        contentPanel.add(fileInfoWrapper);
//
//        // 添加一些水平间隙
//        contentPanel.add(Box.createHorizontalStrut(20));
//
//        // 选择文件按钮
//        JButton selectFileButton = createStyledButton("选择文件", UIUtil.PRIMARY_COLOR);
//        selectFileButton.addActionListener(e -> {
//            JFileChooser fileChooser = new JFileChooser();
//            fileChooser.setDialogTitle("选择题目文件");
//            FileNameExtensionFilter filter = new FileNameExtensionFilter("文本文件 (*.txt)", "txt");
//            fileChooser.setFileFilter(filter);
//
//            int result = fileChooser.showOpenDialog(this);
//            if (result == JFileChooser.APPROVE_OPTION) {
//                selectedImportFile = fileChooser.getSelectedFile();
//                fileNameLabel.setText(selectedImportFile.getName());
//                fileNameLabel.setForeground(UIUtil.PRIMARY_COLOR);
//                filePathLabel.setText(selectedImportFile.getAbsolutePath());
//                closeButton.setVisible(true); // 显示关闭按钮
//            }
//        });
//        contentPanel.add(selectFileButton);
//
//        // 开始导入按钮
//        JButton importButton = createStyledButton("开始导入", UIUtil.SUCCESS_COLOR);
//        importButton.addActionListener(e -> startImport());
//        contentPanel.add(importButton);
//
//        panel.add(contentPanel, BorderLayout.CENTER);
//
//        return panel;
//    }
//
//    /**
//     * 创建格式说明面板
//     */
//    private JPanel createFormatDescriptionPanel() {
//        JPanel panel = new JPanel(new BorderLayout(0, 10));
//        panel.setBackground(Color.WHITE);
//        panel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createTitledBorder(
//                BorderFactory.createLineBorder(new Color(200, 220, 240), 2),
//                "文件格式说明",
//                javax.swing.border.TitledBorder.LEFT,
//                javax.swing.border.TitledBorder.TOP,
//                new Font("微软雅黑", Font.BOLD, 14),
//                UIUtil.PRIMARY_COLOR
//            ),
//            BorderFactory.createEmptyBorder(15, 20, 20, 20)
//        ));
//
//        // 格式说明文本
//        JTextArea formatText = new JTextArea();
//        formatText.setEditable(false);
//        formatText.setFont(new Font("微软雅黑", Font.PLAIN, 12));
//        formatText.setForeground(new Color(80, 80, 80));
//        formatText.setBackground(new Color(248, 250, 252));
//        formatText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//        formatText.setLineWrap(true);
//        formatText.setWrapStyleWord(true);
//
//        String formatInfo = "文件格式：题目类型|科目|题目内容|选项A|选项B|选项C|选项D|正确答案|分值|难度|解析\n\n"
//                + "• 题目类型：SINGLE(单选)、MULTIPLE(多选)、JUDGE(判断)、BLANK(填空)\n"
//                + "• 难度：EASY(简单)、MEDIUM(中等)、HARD(困难)\n"
//                + "• 以#开头的行为注释，会被忽略\n\n"
//                + "示例：\n"
//                + "SINGLE|Java|Java中哪个关键字用于定义常量？|const|final|static|constant|B|5|EASY|解析内容";
//
//        formatText.setText(formatInfo);
//
//        JScrollPane scrollPane = new JScrollPane(formatText);
//        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
//        scrollPane.setPreferredSize(new Dimension(0, 120));
//
//        panel.add(scrollPane, BorderLayout.CENTER);
//
//        return panel;
//    }
//
//    /**
//     * 创建导入操作面板
//     */
//    private JPanel createImportActionPanel() {
//        JPanel panel = new JPanel(new BorderLayout(0, 10));
//        panel.setBackground(Color.WHITE);
//        panel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createTitledBorder(
//                BorderFactory.createLineBorder(new Color(200, 220, 240), 2),
//                "导入操作",
//                javax.swing.border.TitledBorder.LEFT,
//                javax.swing.border.TitledBorder.TOP,
//                new Font("微软雅黑", Font.BOLD, 14),
//                UIUtil.PRIMARY_COLOR
//            ),
//            BorderFactory.createEmptyBorder(10, 20, 15, 20)
//        ));
//
//        // 主内容面板：使用BorderLayout将提示和按钮放在同一行
//        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
//        contentPanel.setBackground(Color.WHITE);
//
//        // 左侧：提示信息
//        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
//        infoPanel.setBackground(new Color(255, 248, 225));
//        infoPanel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createLineBorder(new Color(255, 193, 7)),
//            BorderFactory.createEmptyBorder(8, 12, 8, 12)
//        ));
//
//        JLabel infoIcon = new JLabel("ℹ️");
//        infoIcon.setFont(new Font("微软雅黑", Font.PLAIN, 14));
//
//        JLabel infoLabel = new JLabel("导入前请确保文件格式正确，可先下载模板参考");
//        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
//        infoLabel.setForeground(new Color(102, 77, 3));
//
//        infoPanel.add(infoIcon);
//        infoPanel.add(infoLabel);
//
//        contentPanel.add(infoPanel, BorderLayout.CENTER);
//
//        // 右侧：按钮区域（只保留下载模板按钮）
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
//        buttonPanel.setBackground(Color.WHITE);
//
//        JButton downloadTemplateButton = createStyledButton("下载模板文件", new Color(52, 152, 219));
//        downloadTemplateButton.addActionListener(e -> downloadTemplate());
//
//        buttonPanel.add(downloadTemplateButton);
//
//        contentPanel.add(buttonPanel, BorderLayout.EAST);
//
//        panel.add(contentPanel, BorderLayout.CENTER);
//
//        return panel;
//    }
//
//    /**
//     * 下载模板文件
//     */
//    private void downloadTemplate() {
//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setDialogTitle("保存模板文件");
//        fileChooser.setSelectedFile(new File("题目导入模板.txt"));
//        FileNameExtensionFilter filter = new FileNameExtensionFilter("文本文件 (*.txt)", "txt");
//        fileChooser.setFileFilter(filter);
//
//        int result = fileChooser.showSaveDialog(this);
//        if (result == JFileChooser.APPROVE_OPTION) {
//            File file = fileChooser.getSelectedFile();
//            // 确保文件扩展名为.txt
//            if (!file.getName().toLowerCase().endsWith(".txt")) {
//                file = new File(file.getAbsolutePath() + ".txt");
//            }
//
//            try {
//                QuestionImportUtil.generateTemplate(file);
//                UIUtil.showInfo(this, "模板文件已保存到：\n" + file.getAbsolutePath());
//            } catch (Exception e) {
//                UIUtil.showError(this, "保存模板文件失败：" + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * 开始导入
//     */
//    private void startImport() {
//        if (selectedImportFile == null) {
//            UIUtil.showWarning(this, "请先选择要导入的文件");
//            return;
//        }
//
//        if (!selectedImportFile.exists()) {
//            UIUtil.showError(this, "文件不存在，请重新选择");
//            selectedImportFile = null;
//            return;
//        }
//
//        importQuestionsFromFile(selectedImportFile);
//    }
//
//    private JButton createStyledButton(String text, Color bgColor) {
//        JButton button = new JButton(text);
//        button.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        button.setBackground(bgColor);
//        button.setForeground(Color.BLACK);
//        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
//        button.setFocusPainted(false);
//        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
//        button.setOpaque(true);
//        button.addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mouseEntered(java.awt.event.MouseEvent evt) {
//                button.setBackground(bgColor.darker());
//            }
//            public void mouseExited(java.awt.event.MouseEvent evt) {
//                button.setBackground(bgColor);
//            }
//        });
//        return button;
//    }
//
//    /**
//     * 创建科目按钮
//     */
//    private JButton createSubjectButton(String subject, boolean isActive) {
//        JButton button = new JButton(subject);
//        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
//        button.setHorizontalAlignment(SwingConstants.LEFT);
//        button.setFocusPainted(false);
//        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
//        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
//        button.setContentAreaFilled(false);
//        button.setOpaque(true);
//
//        // 设置图标
//        Icon icon = IconUtil.createCircleIcon(
//            isActive ? UIUtil.PRIMARY_COLOR : new Color(150, 150, 150), 8);
//        button.setIcon(icon);
//        button.setIconTextGap(12);
//        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
//
//        // 设置样式
//        updateSubjectButtonStyle(button, isActive);
//
//        // 悬停效果
//        button.addMouseListener(new java.awt.event.MouseAdapter() {
//            @Override
//            public void mouseEntered(java.awt.event.MouseEvent evt) {
//                if (!button.getBackground().equals(new Color(232, 240, 254))) {
//                    button.setBackground(new Color(245, 245, 245));
//                }
//            }
//
//            @Override
//            public void mouseExited(java.awt.event.MouseEvent evt) {
//                if (!button.getBackground().equals(new Color(232, 240, 254))) {
//                    button.setBackground(new Color(250, 250, 250));
//                }
//            }
//        });
//
//        return button;
//    }
//
//    /**
//     * 更新科目按钮样式
//     */
//    private void updateSubjectButtonStyle(JButton button, boolean isActive) {
//        if (isActive) {
//            button.setBackground(new Color(232, 240, 254));
//            button.setForeground(UIUtil.PRIMARY_COLOR);
//            button.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createMatteBorder(0, 3, 0, 0, UIUtil.PRIMARY_COLOR),
//                BorderFactory.createEmptyBorder(12, 17, 12, 10)
//            ));
//            Icon icon = IconUtil.createCircleIcon(UIUtil.PRIMARY_COLOR, 8);
//            button.setIcon(icon);
//        } else {
//            button.setBackground(new Color(250, 250, 250));
//            button.setForeground(new Color(80, 80, 80));
//            button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
//            Icon icon = IconUtil.createCircleIcon(new Color(150, 150, 150), 8);
//            button.setIcon(icon);
//        }
//    }
//
//    /**
//     * 刷新科目按钮状态
//     */
//    private void refreshSubjectButtons(JPanel categoryPanel) {
//        Component[] components = categoryPanel.getComponents();
//        for (Component comp : components) {
//            if (comp instanceof JButton) {
//                JButton button = (JButton) comp;
//                String buttonText = button.getText();
//                boolean isActive = buttonText.equals(currentSubject);
//                updateSubjectButtonStyle(button, isActive);
//            }
//        }
//    }
//
//    /**
//     * 根据科目加载题目
//     */
//    private void loadQuestionsBySubject(String subject) {
//        tableModel.setRowCount(0);
//        try {
//            List<Question> allQuestions = questionService.getAllQuestions();
//            List<Question> filteredQuestions;
//
//            if ("全部".equals(subject)) {
//                filteredQuestions = allQuestions;
//            } else {
//                filteredQuestions = new ArrayList<>();
//                for (Question q : allQuestions) {
//                    if (subject.equals(q.getSubject())) {
//                        filteredQuestions.add(q);
//                    }
//                }
//            }
//
//            for (Question q : filteredQuestions) {
//                Object[] row = {
//                    q.getSubject(),
//                    q.getQuestionType().getDescription(),
//                    truncate(q.getContent(), 50),
//                    q.getCorrectAnswer(),
//                    "" // 操作列，由渲染器处理
//                };
//                tableModel.addRow(row);
//            }
//
//        } catch (Exception e) {
//            UIUtil.showError(this, "加载题目失败：" + e.getMessage());
//        }
//    }
//
//    private void loadQuestions() {
//        loadQuestionsBySubject(currentSubject);
//    }
//
//    private void showAddQuestionDialog() {
//        JDialog dialog = new JDialog(this, "添加题目", true);
//        dialog.setSize(600, 500);
//        dialog.setLocationRelativeTo(this);
//
//        JPanel panel = new JPanel(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(5, 5, 5, 5);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//
//        // 题目类型
//        gbc.gridx = 0; gbc.gridy = 0;
//        panel.add(new JLabel("题目类型:"), gbc);
//        gbc.gridx = 1;
//        JComboBox<QuestionType> typeCombo = new JComboBox<>(QuestionType.values());
//        panel.add(typeCombo, gbc);
//
//        // 科目
//        gbc.gridx = 0; gbc.gridy = 1;
//        panel.add(new JLabel("科目:"), gbc);
//        gbc.gridx = 1;
//        // 使用下拉框选择科目，去掉"全部"选项
//        String[] subjectOptions = new String[SUBJECTS.length - 1];
//        System.arraycopy(SUBJECTS, 1, subjectOptions, 0, SUBJECTS.length - 1);
//        JComboBox<String> subjectCombo = new JComboBox<>(subjectOptions);
//        subjectCombo.setEditable(true); // 允许输入自定义科目
//        // 根据当前选中的科目设置默认值
//        if ("全部".equals(currentSubject)) {
//            subjectCombo.setSelectedItem("Java"); // 全部时默认选择Java
//        } else {
//            subjectCombo.setSelectedItem(currentSubject); // 选择当前科目
//        }
//        panel.add(subjectCombo, gbc);
//
//        // 题目内容
//        gbc.gridx = 0; gbc.gridy = 2;
//        panel.add(new JLabel("题目内容:"), gbc);
//        gbc.gridx = 1;
//        JTextArea contentArea = new JTextArea(3, 20);
//        contentArea.setLineWrap(true);
//        panel.add(new JScrollPane(contentArea), gbc);
//
//        // 选项A
//        gbc.gridx = 0; gbc.gridy = 3;
//        panel.add(new JLabel("选项A:"), gbc);
//        gbc.gridx = 1;
//        JTextField optionAField = new JTextField(20);
//        panel.add(optionAField, gbc);
//
//        // 选项B
//        gbc.gridx = 0; gbc.gridy = 4;
//        panel.add(new JLabel("选项B:"), gbc);
//        gbc.gridx = 1;
//        JTextField optionBField = new JTextField(20);
//        panel.add(optionBField, gbc);
//
//        // 选项C
//        gbc.gridx = 0; gbc.gridy = 5;
//        panel.add(new JLabel("选项C:"), gbc);
//        gbc.gridx = 1;
//        JTextField optionCField = new JTextField(20);
//        panel.add(optionCField, gbc);
//
//        // 选项D
//        gbc.gridx = 0; gbc.gridy = 6;
//        panel.add(new JLabel("选项D:"), gbc);
//        gbc.gridx = 1;
//        JTextField optionDField = new JTextField(20);
//        panel.add(optionDField, gbc);
//
//        // 正确答案
//        gbc.gridx = 0; gbc.gridy = 7;
//        panel.add(new JLabel("正确答案:"), gbc);
//        gbc.gridx = 1;
//        JTextField answerField = new JTextField(20);
//        panel.add(answerField, gbc);
//
//        // 分值
//        gbc.gridx = 0; gbc.gridy = 8;
//        panel.add(new JLabel("分值:"), gbc);
//        gbc.gridx = 1;
//        JSpinner scoreSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
//        panel.add(scoreSpinner, gbc);
//
//        // 难度
//        gbc.gridx = 0; gbc.gridy = 9;
//        panel.add(new JLabel("难度:"), gbc);
//        gbc.gridx = 1;
//        JComboBox<Difficulty> difficultyCombo = new JComboBox<>(Difficulty.values());
//        panel.add(difficultyCombo, gbc);
//
//        // 按钮
//        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
//        JPanel btnPanel = new JPanel();
//        JButton saveButton = UIUtil.createSuccessButton("保存");
//        JButton cancelButton = UIUtil.createDangerButton("取消");
//
//        saveButton.addActionListener(e -> {
//            try {
//                Question question = new Question();
//                question.setQuestionType((QuestionType) typeCombo.getSelectedItem());
//                // 从下拉框获取科目
//                String selectedSubject = subjectCombo.getSelectedItem() != null
//                    ? subjectCombo.getSelectedItem().toString().trim()
//                    : "";
//                question.setSubject(selectedSubject);
//                question.setContent(contentArea.getText().trim());
//                question.setOptionA(optionAField.getText().trim());
//                question.setOptionB(optionBField.getText().trim());
//                question.setOptionC(optionCField.getText().trim());
//                question.setOptionD(optionDField.getText().trim());
//                question.setCorrectAnswer(answerField.getText().trim());
//                question.setScore((Integer) scoreSpinner.getValue());
//                question.setDifficulty((Difficulty) difficultyCombo.getSelectedItem());
//                question.setCreatorId(teacher.getUserId());
//
//                questionService.addQuestion(question);
//                UIUtil.showInfo(dialog, "添加成功");
//                dialog.dispose();
//                loadQuestions();
//            } catch (Exception ex) {
//                UIUtil.showError(dialog, "添加失败：" + ex.getMessage());
//            }
//        });
//
//        cancelButton.addActionListener(e -> dialog.dispose());
//
//        btnPanel.add(saveButton);
//        btnPanel.add(cancelButton);
//        panel.add(btnPanel, gbc);
//
//        dialog.add(new JScrollPane(panel));
//        dialog.setVisible(true);
//    }
//
//    private void showEditQuestionDialog() {
//        int selectedRow = questionTable.getSelectedRow();
//        if (selectedRow == -1) {
//            UIUtil.showWarning(this, "请先选择要编辑的题目");
//            return;
//        }
//
//        // 根据科目、类型和题目内容查找题目
//        String subject = (String) tableModel.getValueAt(selectedRow, 0);
//        String type = (String) tableModel.getValueAt(selectedRow, 1);
//        String content = (String) tableModel.getValueAt(selectedRow, 2);
//
//        Question question = findQuestionByDetails(subject, type, content);
//        if (question == null) {
//            UIUtil.showError(this, "无法找到对应的题目");
//            return;
//        }
//
//        // 编辑对话框（简化版，与添加类似）
//        UIUtil.showInfo(this, "编辑功能待完善，题目ID: " + question.getQuestionId());
//    }
//
//    private void deleteQuestion() {
//        int selectedRow = questionTable.getSelectedRow();
//        if (selectedRow == -1) {
//            UIUtil.showWarning(this, "请先选择要删除的题目");
//            return;
//        }
//
//        if (!UIUtil.showConfirm(this, "确定要删除这道题目吗？")) {
//            return;
//        }
//
//        try {
//            // 根据科目、类型和题目内容查找题目
//            String subject = (String) tableModel.getValueAt(selectedRow, 0);
//            String type = (String) tableModel.getValueAt(selectedRow, 1);
//            String content = (String) tableModel.getValueAt(selectedRow, 2);
//
//            Question question = findQuestionByDetails(subject, type, content);
//            if (question == null) {
//                UIUtil.showError(this, "无法找到对应的题目");
//                return;
//            }
//
//            questionService.deleteQuestion(question.getQuestionId());
//            UIUtil.showInfo(this, "删除成功");
//            loadQuestions();
//        } catch (Exception e) {
//            UIUtil.showError(this, "删除失败：" + e.getMessage());
//        }
//    }
//
//    /**
//     * 根据科目、类型和题目内容查找题目
//     */
//    private Question findQuestionByDetails(String subject, String type, String contentPrefix) {
//        try {
//            List<Question> allQuestions = questionService.getAllQuestions();
//            for (Question q : allQuestions) {
//                if (q.getSubject().equals(subject)
//                    && q.getQuestionType().getDescription().equals(type)
//                    && (q.getContent().equals(contentPrefix) || q.getContent().startsWith(contentPrefix.replace("...", "")))) {
//                    return q;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private void logout() {
//        if (UIUtil.showConfirm(this, "确定要退出登录吗？")) {
//            dispose();
//            new LoginFrame().setVisible(true);
//        }
//    }
//
//    /**
//     * 显示导入对话框
//     */
//    private void showImportDialog() {
//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setDialogTitle("选择题目文件");
//        FileNameExtensionFilter filter = new FileNameExtensionFilter("文本文件 (*.txt)", "txt");
//        fileChooser.setFileFilter(filter);
//
//        int result = fileChooser.showOpenDialog(this);
//        if (result == JFileChooser.APPROVE_OPTION) {
//            File selectedFile = fileChooser.getSelectedFile();
//            importQuestionsFromFile(selectedFile);
//        }
//    }
//
//    /**
//     * 从文件导入题目
//     */
//    private void importQuestionsFromFile(File file) {
//        try {
//            // 读取题目
//            List<Question> questions = QuestionImportUtil.importFromTextFile(file, teacher.getUserId());
//
//            if (questions.isEmpty()) {
//                UIUtil.showWarning(this, "文件中没有有效的题目数据");
//                return;
//            }
//
//            // 显示确认对话框
//            String message = "成功读取 " + questions.size() + " 道题目\n\n"
//                    + "请选择操作：\n"
//                    + "1. 仅导入题目到题库\n"
//                    + "2. 导入并自动生成试卷";
//
//            Object[] options = {"仅导入题目", "导入并生成试卷", "取消"};
//            int choice = JOptionPane.showOptionDialog(this,
//                    message,
//                    "题目导入",
//                    JOptionPane.YES_NO_CANCEL_OPTION,
//                    JOptionPane.QUESTION_MESSAGE,
//                    null,
//                    options,
//                    options[1]);
//
//            if (choice == 0) {
//                // 仅导入题目
//                importQuestionsOnly(questions);
//            } else if (choice == 1) {
//                // 导入并生成试卷
//                importAndGeneratePaper(questions);
//            }
//
//        } catch (Exception e) {
//            UIUtil.showError(this, "导入失败：" + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 仅导入题目到题库
//     */
//    private void importQuestionsOnly(List<Question> questions) {
//        try {
//            questionService.batchAddQuestions(questions);
//            UIUtil.showInfo(this, "成功导入 " + questions.size() + " 道题目！");
//            loadQuestions();
//        } catch (Exception e) {
//            UIUtil.showError(this, "导入题目失败：" + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 导入题目并生成试卷
//     */
//    private void importAndGeneratePaper(List<Question> questions) {
//        // 显示试卷信息输入对话框
//        JDialog dialog = new JDialog(this, "生成试卷", true);
//        dialog.setSize(450, 350);
//        dialog.setLocationRelativeTo(this);
//
//        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
//        mainPanel.setBackground(new Color(245, 250, 255));
//        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
//
//        // 标题
//        JLabel titleLabel = new JLabel("设置试卷信息");
//        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
//        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
//        mainPanel.add(titleLabel, BorderLayout.NORTH);
//
//        // 表单
//        JPanel formPanel = new JPanel(new GridBagLayout());
//        formPanel.setBackground(Color.WHITE);
//        formPanel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createLineBorder(new Color(220, 220, 220)),
//            BorderFactory.createEmptyBorder(20, 20, 20, 20)
//        ));
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(8, 8, 8, 8);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//
//        // 试卷名称
//        gbc.gridx = 0; gbc.gridy = 0;
//        gbc.weightx = 0;
//        JLabel nameLabel = new JLabel("试卷名称：");
//        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(nameLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        JTextField paperNameField = new JTextField(20);
//        paperNameField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(paperNameField, gbc);
//
//        // 科目
//        gbc.gridx = 0; gbc.gridy = 1;
//        gbc.weightx = 0;
//        JLabel subjectLabel = new JLabel("科　　目：");
//        subjectLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(subjectLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        // 使用下拉框选择科目
//        String[] subjectOptions = new String[SUBJECTS.length - 1];
//        System.arraycopy(SUBJECTS, 1, subjectOptions, 0, SUBJECTS.length - 1);
//        JComboBox<String> subjectCombo = new JComboBox<>(subjectOptions);
//        subjectCombo.setEditable(true);
//        subjectCombo.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        // 自动填充科目（从题目中获取）
//        if (!questions.isEmpty()) {
//            subjectCombo.setSelectedItem(questions.get(0).getSubject());
//        }
//        formPanel.add(subjectCombo, gbc);
//
//        // 考试时长
//        gbc.gridx = 0; gbc.gridy = 2;
//        gbc.weightx = 0;
//        JLabel durationLabel = new JLabel("时长(分钟)：");
//        durationLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(durationLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(90, 10, 300, 10));
//        durationSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(durationSpinner, gbc);
//
//        // 及格分数
//        gbc.gridx = 0; gbc.gridy = 3;
//        gbc.weightx = 0;
//        JLabel passScoreLabel = new JLabel("及格分数：");
//        passScoreLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(passScoreLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        JSpinner passScoreSpinner = new JSpinner(new SpinnerNumberModel(60, 0, 100, 5));
//        passScoreSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(passScoreSpinner, gbc);
//
//        // 描述
//        gbc.gridx = 0; gbc.gridy = 4;
//        gbc.weightx = 0;
//        JLabel descLabel = new JLabel("描　　述：");
//        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(descLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        JTextArea descArea = new JTextArea(3, 20);
//        descArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        descArea.setLineWrap(true);
//        descArea.setWrapStyleWord(true);
//        JScrollPane descScroll = new JScrollPane(descArea);
//        formPanel.add(descScroll, gbc);
//
//        mainPanel.add(formPanel, BorderLayout.CENTER);
//
//        // 按钮
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
//        buttonPanel.setBackground(new Color(245, 250, 255));
//
//        JButton confirmButton = createStyledButton("生成试卷", UIUtil.PRIMARY_COLOR);
//        confirmButton.addActionListener(e -> {
//            String paperName = paperNameField.getText().trim();
//            String subject = subjectCombo.getSelectedItem() != null
//                ? subjectCombo.getSelectedItem().toString().trim()
//                : "";
//
//            if (paperName.isEmpty()) {
//                UIUtil.showWarning(dialog, "试卷名称不能为空");
//                return;
//            }
//            if (subject.isEmpty()) {
//                UIUtil.showWarning(dialog, "科目不能为空");
//                return;
//            }
//
//            try {
//                // 先导入题目
//                List<Integer> questionIds = questionService.batchAddQuestions(questions);
//
//                // 创建试卷
//                Paper paper = new Paper();
//                paper.setPaperName(paperName);
//                paper.setSubject(subject);
//                paper.setDuration((Integer) durationSpinner.getValue());
//                paper.setPassScore((Integer) passScoreSpinner.getValue());
//                paper.setDescription(descArea.getText().trim());
//                paper.setCreatorId(teacher.getUserId());
//
//                int paperId = paperService.createPaper(paper, questionIds);
//
//                UIUtil.showInfo(dialog, "成功生成试卷！\n导入题目：" + questions.size() + " 道");
//                dialog.dispose();
//                loadQuestions();
//
//            } catch (Exception ex) {
//                UIUtil.showError(dialog, "生成试卷失败：" + ex.getMessage());
//                ex.printStackTrace();
//            }
//        });
//
//        JButton cancelButton = createStyledButton("取消", new Color(120, 144, 156));
//        cancelButton.addActionListener(e -> dialog.dispose());
//
//        buttonPanel.add(confirmButton);
//        buttonPanel.add(cancelButton);
//
//        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
//
//        dialog.add(mainPanel);
//        dialog.setVisible(true);
//    }
//
//    private String truncate(String text, int maxLength) {
//        if (text == null) return "";
//        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
//    }
//
//    /**
//     * 表格操作列按钮面板渲染器
//     */
//    private class ButtonPanelRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
//        private JButton editButton;
//        private JButton deleteButton;
//
//        public ButtonPanelRenderer() {
//            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
//            setBackground(Color.WHITE);
//
//            editButton = new JButton("编辑");
//            editButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
//            editButton.setBackground(UIUtil.PRIMARY_COLOR);
//            editButton.setForeground(Color.BLACK);
//            editButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
//            editButton.setFocusPainted(false);
//            editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//
//            deleteButton = new JButton("删除");
//            deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
//            deleteButton.setBackground(UIUtil.DANGER_COLOR);
//            deleteButton.setForeground(Color.BLACK);
//            deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
//            deleteButton.setFocusPainted(false);
//            deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//
//            add(editButton);
//            add(deleteButton);
//        }
//
//        @Override
//        public Component getTableCellRendererComponent(JTable table, Object value,
//                boolean isSelected, boolean hasFocus, int row, int column) {
//            if (isSelected) {
//                setBackground(table.getSelectionBackground());
//            } else {
//                setBackground(Color.WHITE);
//            }
//            return this;
//        }
//    }
//
//    /**
//     * 表格操作列按钮面板编辑器
//     */
//    private class ButtonPanelEditor extends DefaultCellEditor {
//        private JPanel panel;
//        private JButton editButton;
//        private JButton deleteButton;
//        private int currentRow;
//        private JTable table;
//
//        public ButtonPanelEditor(JTable table) {
//            super(new JCheckBox());
//            this.table = table;
//
//            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
//            panel.setBackground(Color.WHITE);
//
//            editButton = new JButton("编辑");
//            editButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
//            editButton.setBackground(UIUtil.PRIMARY_COLOR);
//            editButton.setForeground(Color.BLACK);
//            editButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
//            editButton.setFocusPainted(false);
//            editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//            editButton.addActionListener(e -> {
//                fireEditingStopped();
//                editQuestionAtRow(currentRow);
//            });
//
//            deleteButton = new JButton("删除");
//            deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
//            deleteButton.setBackground(UIUtil.DANGER_COLOR);
//            deleteButton.setForeground(Color.BLACK);
//            deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
//            deleteButton.setFocusPainted(false);
//            deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//            deleteButton.addActionListener(e -> {
//                fireEditingStopped();
//                deleteQuestionAtRow(currentRow);
//            });
//
//            panel.add(editButton);
//            panel.add(deleteButton);
//        }
//
//        @Override
//        public Component getTableCellEditorComponent(JTable table, Object value,
//                boolean isSelected, int row, int column) {
//            currentRow = row;
//            if (isSelected) {
//                panel.setBackground(table.getSelectionBackground());
//            } else {
//                panel.setBackground(Color.WHITE);
//            }
//            return panel;
//        }
//
//        @Override
//        public Object getCellEditorValue() {
//            return "";
//        }
//    }
//
//    /**
//     * 编辑指定行的题目
//     */
//    private void editQuestionAtRow(int row) {
//        String subject = (String) tableModel.getValueAt(row, 0);
//        String type = (String) tableModel.getValueAt(row, 1);
//        String content = (String) tableModel.getValueAt(row, 2);
//
//        Question question = findQuestionByDetails(subject, type, content);
//        if (question == null) {
//            UIUtil.showError(this, "无法找到对应的题目");
//            return;
//        }
//
//        // 创建编辑对话框
//        JDialog dialog = new JDialog(this, "编辑题目", true);
//        dialog.setSize(600, 500);
//        dialog.setLocationRelativeTo(this);
//
//        JPanel panel = new JPanel(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(5, 5, 5, 5);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//
//        // 题目类型
//        gbc.gridx = 0; gbc.gridy = 0;
//        panel.add(new JLabel("题目类型:"), gbc);
//        gbc.gridx = 1;
//        JComboBox<QuestionType> typeCombo = new JComboBox<>(QuestionType.values());
//        typeCombo.setSelectedItem(question.getQuestionType());
//        panel.add(typeCombo, gbc);
//
//        // 科目
//        gbc.gridx = 0; gbc.gridy = 1;
//        panel.add(new JLabel("科目:"), gbc);
//        gbc.gridx = 1;
//        String[] subjectOptions = new String[SUBJECTS.length - 1];
//        System.arraycopy(SUBJECTS, 1, subjectOptions, 0, SUBJECTS.length - 1);
//        JComboBox<String> subjectCombo = new JComboBox<>(subjectOptions);
//        subjectCombo.setEditable(true);
//        subjectCombo.setSelectedItem(question.getSubject());
//        panel.add(subjectCombo, gbc);
//
//        // 题目内容
//        gbc.gridx = 0; gbc.gridy = 2;
//        panel.add(new JLabel("题目内容:"), gbc);
//        gbc.gridx = 1;
//        JTextArea contentArea = new JTextArea(3, 20);
//        contentArea.setLineWrap(true);
//        contentArea.setText(question.getContent());
//        panel.add(new JScrollPane(contentArea), gbc);
//
//        // 选项A
//        gbc.gridx = 0; gbc.gridy = 3;
//        panel.add(new JLabel("选项A:"), gbc);
//        gbc.gridx = 1;
//        JTextField optionAField = new JTextField(20);
//        optionAField.setText(question.getOptionA());
//        panel.add(optionAField, gbc);
//
//        // 选项B
//        gbc.gridx = 0; gbc.gridy = 4;
//        panel.add(new JLabel("选项B:"), gbc);
//        gbc.gridx = 1;
//        JTextField optionBField = new JTextField(20);
//        optionBField.setText(question.getOptionB());
//        panel.add(optionBField, gbc);
//
//        // 选项C
//        gbc.gridx = 0; gbc.gridy = 5;
//        panel.add(new JLabel("选项C:"), gbc);
//        gbc.gridx = 1;
//        JTextField optionCField = new JTextField(20);
//        optionCField.setText(question.getOptionC());
//        panel.add(optionCField, gbc);
//
//        // 选项D
//        gbc.gridx = 0; gbc.gridy = 6;
//        panel.add(new JLabel("选项D:"), gbc);
//        gbc.gridx = 1;
//        JTextField optionDField = new JTextField(20);
//        optionDField.setText(question.getOptionD());
//        panel.add(optionDField, gbc);
//
//        // 正确答案
//        gbc.gridx = 0; gbc.gridy = 7;
//        panel.add(new JLabel("正确答案:"), gbc);
//        gbc.gridx = 1;
//        JTextField answerField = new JTextField(20);
//        answerField.setText(question.getCorrectAnswer());
//        panel.add(answerField, gbc);
//
//        // 分值
//        gbc.gridx = 0; gbc.gridy = 8;
//        panel.add(new JLabel("分值:"), gbc);
//        gbc.gridx = 1;
//        int currentScore = question.getScore() != null ? question.getScore() : 5;
//        JSpinner scoreSpinner = new JSpinner(new SpinnerNumberModel(currentScore, 1, 100, 1));
//        panel.add(scoreSpinner, gbc);
//
//        // 难度
//        gbc.gridx = 0; gbc.gridy = 9;
//        panel.add(new JLabel("难度:"), gbc);
//        gbc.gridx = 1;
//        JComboBox<Difficulty> difficultyCombo = new JComboBox<>(Difficulty.values());
//        difficultyCombo.setSelectedItem(question.getDifficulty());
//        panel.add(difficultyCombo, gbc);
//
//        // 按钮
//        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
//        JPanel btnPanel = new JPanel();
//        JButton saveButton = UIUtil.createSuccessButton("保存");
//        JButton cancelButton = UIUtil.createDangerButton("取消");
//
//        saveButton.addActionListener(e -> {
//            try {
//                question.setQuestionType((QuestionType) typeCombo.getSelectedItem());
//                String selectedSubject = subjectCombo.getSelectedItem() != null
//                    ? subjectCombo.getSelectedItem().toString().trim()
//                    : "";
//                question.setSubject(selectedSubject);
//                question.setContent(contentArea.getText().trim());
//                question.setOptionA(optionAField.getText().trim());
//                question.setOptionB(optionBField.getText().trim());
//                question.setOptionC(optionCField.getText().trim());
//                question.setOptionD(optionDField.getText().trim());
//                question.setCorrectAnswer(answerField.getText().trim());
//                question.setScore((Integer) scoreSpinner.getValue());
//                question.setDifficulty((Difficulty) difficultyCombo.getSelectedItem());
//
//                questionService.updateQuestion(question);
//                UIUtil.showInfo(dialog, "修改成功");
//                dialog.dispose();
//                loadQuestions();
//            } catch (Exception ex) {
//                UIUtil.showError(dialog, "修改失败：" + ex.getMessage());
//            }
//        });
//
//        cancelButton.addActionListener(e -> dialog.dispose());
//
//        btnPanel.add(saveButton);
//        btnPanel.add(cancelButton);
//        panel.add(btnPanel, gbc);
//
//        dialog.add(new JScrollPane(panel));
//        dialog.setVisible(true);
//    }
//
//    /**
//     * 删除指定行的题目
//     */
//    private void deleteQuestionAtRow(int row) {
//        if (!UIUtil.showConfirm(this, "确定要删除这道题目吗？")) {
//            return;
//        }
//
//        try {
//            String subject = (String) tableModel.getValueAt(row, 0);
//            String type = (String) tableModel.getValueAt(row, 1);
//            String content = (String) tableModel.getValueAt(row, 2);
//
//            Question question = findQuestionByDetails(subject, type, content);
//            if (question == null) {
//                UIUtil.showError(this, "无法找到对应的题目");
//                return;
//            }
//
//            questionService.deleteQuestion(question.getQuestionId());
//            UIUtil.showInfo(this, "删除成功");
//            loadQuestions();
//        } catch (Exception e) {
//            UIUtil.showError(this, "删除失败：" + e.getMessage());
//        }
//    }
//
//    // ========================================
//    // 试卷管理相关方法（剩余部分）
//    // ========================================
//
//    /**
//     * 编辑指定行的试卷
//     */
//    private void editPaperAtRow(int row) {
//        String paperName = (String) paperTableModel.getValueAt(row, 0);
//
//        try {
//            Paper paper = paperService.getPaperByName(paperName);
//            if (paper == null) {
//                UIUtil.showError(this, "无法找到对应的试卷");
//                return;
//            }
//
//            showEditPaperDialog(paper);
//        } catch (Exception e) {
//            UIUtil.showError(this, "加载试卷信息失败：" + e.getMessage());
//        }
//    }
//
//    /**
//     * 显示编辑试卷对话框
//     */
//    private void showEditPaperDialog(Paper paper) {
//        JDialog dialog = new JDialog(this, "编辑试卷", true);
//        dialog.setSize(700, 600);
//        dialog.setLocationRelativeTo(this);
//
//        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
//        mainPanel.setBackground(new Color(245, 250, 255));
//        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
//
//        // 标题
//        JLabel titleLabel = new JLabel("编辑试卷");
//        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
//        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
//        mainPanel.add(titleLabel, BorderLayout.NORTH);
//
//        // 表单
//        JPanel formPanel = new JPanel(new GridBagLayout());
//        formPanel.setBackground(Color.WHITE);
//        formPanel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createLineBorder(new Color(220, 220, 220)),
//            BorderFactory.createEmptyBorder(20, 20, 20, 20)
//        ));
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(8, 8, 8, 8);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//
//        // 试卷名称
//        gbc.gridx = 0; gbc.gridy = 0;
//        gbc.weightx = 0;
//        JLabel nameLabel = new JLabel("试卷名称：");
//        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(nameLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        JTextField paperNameField = new JTextField(20);
//        paperNameField.setText(paper.getPaperName());
//        paperNameField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(paperNameField, gbc);
//
//        // 科目
//        gbc.gridx = 0; gbc.gridy = 1;
//        gbc.weightx = 0;
//        JLabel subjectLabel = new JLabel("科　　目：");
//        subjectLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(subjectLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        String[] subjectOptions = new String[SUBJECTS.length - 1];
//        System.arraycopy(SUBJECTS, 1, subjectOptions, 0, SUBJECTS.length - 1);
//        JComboBox<String> subjectCombo = new JComboBox<>(subjectOptions);
//        subjectCombo.setSelectedItem(paper.getSubject());
//        subjectCombo.setEditable(true);
//        subjectCombo.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(subjectCombo, gbc);
//
//        // 考试时长
//        gbc.gridx = 0; gbc.gridy = 2;
//        gbc.weightx = 0;
//        JLabel durationLabel = new JLabel("时长(分钟)：");
//        durationLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(durationLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(paper.getDuration().intValue(), 10, 300, 10));
//        durationSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(durationSpinner, gbc);
//
//        // 及格分数
//        gbc.gridx = 0; gbc.gridy = 3;
//        gbc.weightx = 0;
//        JLabel passScoreLabel = new JLabel("及格分数：");
//        passScoreLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(passScoreLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        JSpinner passScoreSpinner = new JSpinner(new SpinnerNumberModel(paper.getPassScore().intValue(), 0, 100, 5));
//        passScoreSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(passScoreSpinner, gbc);
//
//        // 描述
//        gbc.gridx = 0; gbc.gridy = 4;
//        gbc.weightx = 0;
//        JLabel descLabel = new JLabel("描　　述：");
//        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(descLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        JTextArea descArea = new JTextArea(3, 20);
//        descArea.setText(paper.getDescription());
//        descArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        descArea.setLineWrap(true);
//        descArea.setWrapStyleWord(true);
//        JScrollPane descScroll = new JScrollPane(descArea);
//        formPanel.add(descScroll, gbc);
//
//        // 题目信息（显示，不可修改）
//        gbc.gridx = 0; gbc.gridy = 5;
//        gbc.weightx = 0;
//        JLabel questionsLabel = new JLabel("题目数量：");
//        questionsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        formPanel.add(questionsLabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        int questionCount = paper.getQuestions() != null ? paper.getQuestions().size() : 0;
//        JLabel countLabel = new JLabel(questionCount + " 道题目（总分：" + paper.getTotalScore() + "分）");
//        countLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        countLabel.setForeground(new Color(100, 100, 100));
//        formPanel.add(countLabel, gbc);
//
//        mainPanel.add(formPanel, BorderLayout.CENTER);
//
//        // 按钮
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
//        buttonPanel.setBackground(new Color(245, 250, 255));
//
//        JButton saveButton = createStyledButton("保存修改", UIUtil.PRIMARY_COLOR);
//        saveButton.addActionListener(e -> {
//            String paperName = paperNameField.getText().trim();
//            String subject = subjectCombo.getSelectedItem() != null
//                ? subjectCombo.getSelectedItem().toString().trim()
//                : "";
//
//            if (paperName.isEmpty()) {
//                UIUtil.showWarning(dialog, "试卷名称不能为空");
//                return;
//            }
//            if (subject.isEmpty()) {
//                UIUtil.showWarning(dialog, "科目不能为空");
//                return;
//            }
//
//            try {
//                paper.setPaperName(paperName);
//                paper.setSubject(subject);
//                paper.setDuration((Integer) durationSpinner.getValue());
//                paper.setPassScore((Integer) passScoreSpinner.getValue());
//                paper.setDescription(descArea.getText().trim());
//
//                paperService.updatePaper(paper);
//
//                UIUtil.showInfo(dialog, "试卷修改成功！");
//                dialog.dispose();
//                loadPapersData();
//
//            } catch (Exception ex) {
//                UIUtil.showError(dialog, "修改试卷失败：" + ex.getMessage());
//                ex.printStackTrace();
//            }
//        });
//
//        JButton cancelButton = createStyledButton("取消", new Color(120, 144, 156));
//        cancelButton.addActionListener(e -> dialog.dispose());
//
//        buttonPanel.add(saveButton);
//        buttonPanel.add(cancelButton);
//
//        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
//
//        dialog.add(mainPanel);
//        dialog.setVisible(true);
//    }
//
//    /**
//     * 查看指定行试卷的详细信息
//     */
//    private void viewPaperAtRow(int row) {
//        String paperName = (String) paperTableModel.getValueAt(row, 0);
//
//        try {
//            Paper paper = paperService.getPaperByName(paperName);
//            if (paper == null) {
//                UIUtil.showError(this, "无法找到对应的试卷");
//                return;
//            }
//
//            showPaperDetailDialog(paper);
//        } catch (Exception e) {
//            UIUtil.showError(this, "加载试卷信息失败：" + e.getMessage());
//        }
//    }
//
//    /**
//     * 显示试卷详细信息对话框
//     */
//    private void showPaperDetailDialog(Paper paper) {
//        JDialog dialog = new JDialog(this, "试卷详情", true);
//        dialog.setSize(800, 600);
//        dialog.setLocationRelativeTo(this);
//
//        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
//        mainPanel.setBackground(Color.WHITE);
//        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
//
//        // 标题
//        JLabel titleLabel = new JLabel("试卷详细信息");
//        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
//        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
//        mainPanel.add(titleLabel, BorderLayout.NORTH);
//
//        // 试卷基本信息
//        JPanel infoPanel = new JPanel(new GridLayout(6, 2, 10, 10));
//        infoPanel.setBackground(Color.WHITE);
//        infoPanel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createTitledBorder("基本信息"),
//            BorderFactory.createEmptyBorder(10, 10, 10, 10)
//        ));
//
//        infoPanel.add(createInfoLabel("试卷名称："));
//        infoPanel.add(createInfoValueLabel(paper.getPaperName()));
//
//        infoPanel.add(createInfoLabel("科目："));
//        infoPanel.add(createInfoValueLabel(paper.getSubject()));
//
//        infoPanel.add(createInfoLabel("题目数量："));
//        int questionCount = paper.getQuestions() != null ? paper.getQuestions().size() : 0;
//        infoPanel.add(createInfoValueLabel(questionCount + " 道"));
//
//        infoPanel.add(createInfoLabel("总分："));
//        infoPanel.add(createInfoValueLabel(paper.getTotalScore() + " 分"));
//
//        infoPanel.add(createInfoLabel("考试时长："));
//        infoPanel.add(createInfoValueLabel(paper.getDuration() + " 分钟"));
//
//        infoPanel.add(createInfoLabel("及格分数："));
//        infoPanel.add(createInfoValueLabel(paper.getPassScore() + " 分"));
//
//        // 题目列表
//        JPanel questionsPanel = new JPanel(new BorderLayout(0, 10));
//        questionsPanel.setBackground(Color.WHITE);
//        questionsPanel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createTitledBorder("题目列表"),
//            BorderFactory.createEmptyBorder(10, 10, 10, 10)
//        ));
//
//        String[] columns = {"序号", "类型", "题目内容", "分值"};
//        DefaultTableModel questionTableModel = new DefaultTableModel(columns, 0) {
//            @Override
//            public boolean isCellEditable(int row, int column) {
//                return false;
//            }
//        };
//
//        if (paper.getQuestions() != null) {
//            int index = 1;
//            for (Question q : paper.getQuestions()) {
//                Object[] row = {
//                    index++,
//                    q.getQuestionType().getDescription(),
//                    truncate(q.getContent(), 60),
//                    q.getScore() + "分"
//                };
//                questionTableModel.addRow(row);
//            }
//        }
//
//        JTable questionTable = new JTable(questionTableModel);
//        questionTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        questionTable.setRowHeight(35);
//        questionTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
//        questionTable.getTableHeader().setBackground(new Color(245, 247, 250));
//
//        JScrollPane scrollPane = new JScrollPane(questionTable);
//        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
//        questionsPanel.add(scrollPane, BorderLayout.CENTER);
//
//        // 组合面板
//        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
//        centerPanel.setBackground(Color.WHITE);
//        centerPanel.add(infoPanel, BorderLayout.NORTH);
//        centerPanel.add(questionsPanel, BorderLayout.CENTER);
//
//        mainPanel.add(centerPanel, BorderLayout.CENTER);
//
//        // 关闭按钮
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//        buttonPanel.setBackground(Color.WHITE);
//
//        JButton closeButton = createStyledButton("关闭", UIUtil.PRIMARY_COLOR);
//        closeButton.addActionListener(e -> dialog.dispose());
//        buttonPanel.add(closeButton);
//
//        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
//
//        dialog.add(mainPanel);
//        dialog.setVisible(true);
//    }
//
//    private JLabel createInfoLabel(String text) {
//        JLabel label = new JLabel(text);
//        label.setFont(new Font("微软雅黑", Font.PLAIN, 13));
//        label.setForeground(new Color(100, 100, 100));
//        return label;
//    }
//
//    private JLabel createInfoValueLabel(String text) {
//        JLabel label = new JLabel(text);
//        label.setFont(new Font("微软雅黑", Font.BOLD, 13));
//        label.setForeground(UIUtil.TEXT_COLOR);
//        return label;
//    }
//
//    /**
//     * 删除指定行的试卷
//     */
//    private void deletePaperAtRow(int row) {
//        if (!UIUtil.showConfirm(this, "确定要删除这份试卷吗？\n删除后将无法恢复！")) {
//            return;
//        }
//
//        try {
//            String paperName = (String) paperTableModel.getValueAt(row, 0);
//            Paper paper = paperService.getPaperByName(paperName);
//
//            if (paper == null) {
//                UIUtil.showError(this, "无法找到对应的试卷");
//                return;
//            }
//
//            // 先删除试卷题目关联
//            paperDao.deletePaperQuestions(paper.getPaperId());
//            // 再删除试卷
//            paperService.deletePaper(paper.getPaperId());
//
//            UIUtil.showInfo(this, "删除成功");
//            loadPapersData();
//        } catch (Exception e) {
//            UIUtil.showError(this, "删除失败：" + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 试卷表格操作列按钮面板渲染器
//     */
//    private class PaperButtonPanelRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
//        private JButton viewButton;
//        private JButton editButton;
//        private JButton deleteButton;
//        private JButton publishButton;
//
//        public PaperButtonPanelRenderer() {
//            setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
//            setBackground(Color.WHITE);
//
//            viewButton = new JButton("查看");
//            viewButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
//            viewButton.setBackground(new Color(52, 152, 219));
//            viewButton.setForeground(Color.BLACK);
//            viewButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
//            viewButton.setFocusPainted(false);
//            viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//
//            editButton = new JButton("编辑");
//            editButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
//            editButton.setBackground(UIUtil.PRIMARY_COLOR);
//            editButton.setForeground(Color.BLACK);
//            editButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
//            editButton.setFocusPainted(false);
//            editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//
//            deleteButton = new JButton("删除");
//            deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
//            deleteButton.setBackground(UIUtil.DANGER_COLOR);
//            deleteButton.setForeground(Color.BLACK);
//            deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
//            deleteButton.setFocusPainted(false);
//            deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//
//            publishButton = new JButton("发布");
//            publishButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
//            publishButton.setBackground(UIUtil.SUCCESS_COLOR);
//            publishButton.setForeground(Color.BLACK);
//            publishButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
//            publishButton.setFocusPainted(false);
//            publishButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//
//            add(viewButton);
//            add(editButton);
//            add(publishButton);
//            add(deleteButton);
//        }
//
//        @Override
//        public Component getTableCellRendererComponent(JTable table, Object value,
//                boolean isSelected, boolean hasFocus, int row, int column) {
//            if (isSelected) {
//                setBackground(table.getSelectionBackground());
//            } else {
//                setBackground(Color.WHITE);
//            }
//            return this;
//        }
//    }
//
//    /**
//     * 试卷表格操作列按钮面板编辑器
//     */
//    private class PaperButtonPanelEditor extends DefaultCellEditor {
//        private JPanel panel;
//        private JButton viewButton;
//        private JButton editButton;
//        private JButton deleteButton;
//        private JButton publishButton;
//        private int currentRow;
//        private JTable table;
//
//        public PaperButtonPanelEditor(JTable table) {
//            super(new JCheckBox());
//            this.table = table;
//
//            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
//            panel.setBackground(Color.WHITE);
//
//            viewButton = new JButton("查看");
//            viewButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
//            viewButton.setBackground(new Color(52, 152, 219));
//            viewButton.setForeground(Color.BLACK);
//            viewButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
//            viewButton.setFocusPainted(false);
//            viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//            viewButton.addActionListener(e -> {
//                fireEditingStopped();
//                viewPaperAtRow(currentRow);
//            });
//
//            editButton = new JButton("编辑");
//            editButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
//            editButton.setBackground(UIUtil.PRIMARY_COLOR);
//            editButton.setForeground(Color.BLACK);
//            editButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
//            editButton.setFocusPainted(false);
//            editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//            editButton.addActionListener(e -> {
//                fireEditingStopped();
//                editPaperAtRow(currentRow);
//            });
//
//            deleteButton = new JButton("删除");
//            deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
//            deleteButton.setBackground(UIUtil.DANGER_COLOR);
//            deleteButton.setForeground(Color.BLACK);
//            deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
//            deleteButton.setFocusPainted(false);
//            deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//            deleteButton.addActionListener(e -> {
//                fireEditingStopped();
//                deletePaperAtRow(currentRow);
//            });
//
//            publishButton = new JButton("发布");
//            publishButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
//            publishButton.setBackground(UIUtil.SUCCESS_COLOR);
//            publishButton.setForeground(Color.BLACK);
//            publishButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
//            publishButton.setFocusPainted(false);
//            publishButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//            publishButton.addActionListener(e -> {
//                fireEditingStopped();
//                togglePublishAtRow(currentRow);
//            });
//
//            panel.add(viewButton);
//            panel.add(editButton);
//            panel.add(publishButton);
//            panel.add(deleteButton);
//        }
//
//        @Override
//        public Component getTableCellEditorComponent(JTable table, Object value,
//                boolean isSelected, int row, int column) {
//            currentRow = row;
//
//            // 根据试卷发布状态更新按钮文字和颜色
//            try {
//                String paperName = (String) paperTableModel.getValueAt(row, 0);
//                Paper paper = paperService.getPaperByName(paperName);
//                if (paper != null && paper.getIsPublished() != null && paper.getIsPublished()) {
//                    publishButton.setText("取消发布");
//                    publishButton.setBackground(new Color(255, 152, 0));
//                } else {
//                    publishButton.setText("发布");
//                    publishButton.setBackground(UIUtil.SUCCESS_COLOR);
//                }
//            } catch (Exception ex) {
//                // 如果获取失败，使用默认状态
//                publishButton.setText("发布");
//                publishButton.setBackground(UIUtil.SUCCESS_COLOR);
//            }
//
//            if (isSelected) {
//                panel.setBackground(table.getSelectionBackground());
//            } else {
//                panel.setBackground(Color.WHITE);
//            }
//            return panel;
//        }
//
//        @Override
//        public Object getCellEditorValue() {
//            return "";
//        }
//    }
//
//    /**
//     * 切换试卷发布状态
//     */
//    private void togglePublishAtRow(int row) {
//        String paperName = (String) paperTableModel.getValueAt(row, 0);
//        String action = ""; // 声明在外部，以便在catch块中使用
//
//        try {
//            Paper paper = paperService.getPaperByName(paperName);
//            if (paper == null) {
//                UIUtil.showError(this, "无法找到对应的试卷");
//                return;
//            }
//
//            boolean currentStatus = paper.getIsPublished() != null && paper.getIsPublished();
//            action = currentStatus ? "取消发布" : "发布";
//
//            int confirm = JOptionPane.showConfirmDialog(
//                this,
//                "确定要" + action + "试卷《" + paperName + "》吗？",
//                action + "确认",
//                JOptionPane.YES_NO_OPTION
//            );
//
//            if (confirm == JOptionPane.YES_OPTION) {
//                if (currentStatus) {
//                    paperService.unpublishPaper(paper.getPaperId());
//                    UIUtil.showInfo(this, "试卷已取消发布");
//                } else {
//                    paperService.publishPaper(paper.getPaperId());
//                    UIUtil.showInfo(this, "试卷已发布，学生端现在可以看到该试卷了");
//                }
//                loadPapersData(); // 刷新数据
//            }
//        } catch (Exception e) {
//            UIUtil.showError(this, action + "失败：" + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}