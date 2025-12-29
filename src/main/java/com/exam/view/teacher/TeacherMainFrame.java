package com.exam.view.teacher;

import com.exam.model.User;
import com.exam.model.Question;
import com.exam.model.Paper;
import com.exam.model.enums.QuestionType;
import com.exam.model.enums.Difficulty;
import com.exam.service.QuestionService;
import com.exam.service.PaperService;
import com.exam.util.UIUtil;
import com.exam.util.IconUtil;
import com.exam.util.QuestionImportUtil;
// import com.exam.view.LoginFrame; // 已删除,使用TeacherLoginFrame
import com.exam.view.teacher.ui.components.*;
import com.exam.view.teacher.manager.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * 教师主界面
 */
public class TeacherMainFrame extends JFrame {
    private final User teacher;
    private final QuestionService questionService;
    private final PaperService paperService;
    
    private JPanel mainContentPanel;
    private String currentView = "home";
    private List<JButton> menuButtons = new ArrayList<>();
    
    // 管理器
    private com.exam.view.teacher.manager.QuestionManager questionManager;
    private com.exam.view.teacher.manager.PaperManager paperManager;
    private com.exam.view.teacher.manager.ImportManager importManager;
    
    // 面板缓存（懒加载）
    private TeacherHomePanel homePanel;
    private TeacherQuestionPanel questionPanel;
    private TeacherPaperPanel paperPanel;
    private TeacherImportPanel importPanel;
    private TeacherStudentPanel studentPanel;
    
    // 题库管理相关变量（废弃，已迁移到TeacherQuestionPanel）
    private JTable questionTable;
    private DefaultTableModel tableModel;
    private String currentSubject = "全部";

    public TeacherMainFrame(User teacher) {
        this.teacher = teacher;
        this.questionService = new QuestionService();
        this.paperService = new PaperService();
        this.questionManager = new com.exam.view.teacher.manager.QuestionManager(questionService, this);
        this.paperManager = new com.exam.view.teacher.manager.PaperManager(paperService, this);
        this.importManager = new com.exam.view.teacher.manager.ImportManager(questionService, this);
        initComponents();
        setTitle("未来教育考试系统 - 教师端");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UIUtil.centerWindow(this);
    }
    
    /**
     * 公共方法：切换到指定视图
     */
    public void switchToView(String view) {
        switchView(view);
    }
    
    /**
     * 获取教师对象
     */
    public User getTeacher() {
        return teacher;
    }
    
    /**
     * 获取QuestionService
     */
    public QuestionService getQuestionService() {
        return questionService;
    }
    
    /**
     * 获取PaperService
     */
    public PaperService getPaperService() {
        return paperService;
    }
    
    /**
     * 刷新题库数据
     */
    public void refreshQuestionData() {
        System.out.println("DEBUG: TeacherMainFrame.refreshQuestionData() called");
        if (questionPanel != null) {
            System.out.println("DEBUG: Calling questionPanel.refreshData()");
            questionPanel.refreshData();
        } else {
            System.out.println("DEBUG: questionPanel is null");
        }
    }
    
    /**
     * 刷新试卷数据
     */
    public void refreshPaperData() {
        System.out.println("DEBUG: TeacherMainFrame.refreshPaperData() called");
        if (paperPanel != null) {
            System.out.println("DEBUG: Calling paperPanel.refreshData()");
            paperPanel.refreshData();
        } else {
            System.out.println("DEBUG: paperPanel is null, but still need to ensure data is updated");
            // 即使paperPanel未初始化，也要确保试卷数据被更新
            // 如果用户切换到试卷管理面板时，需要显示最新数据
            // 我们可以提前初始化paperPanel或确保数据在需要时被加载
            if ("paper".equals(currentView)) {
                // 如果当前已经在试卷管理页面，但panel还没初始化，我们需要强制创建
                switchView("paper");
            }
        }
    }
    
    /**
     * 获取questionPanel
     */
    public TeacherQuestionPanel getQuestionPanel() {
        return questionPanel;
    }
    
    /**
     * 获取paperPanel
     */
    public TeacherPaperPanel getPaperPanel() {
        return paperPanel;
    }
    
    /**
     * 获取importPanel
     */
    public TeacherImportPanel getImportPanel() {
        return importPanel;
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(UIUtil.BACKGROUND_COLOR);

        // 顶部面板 - 与学生界面一致
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

        JLabel welcomeLabel = new JLabel("欢迎，" + teacher.getRealName() + " 老师");
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
        homePanel = new TeacherHomePanel(this, teacher);
        mainContentPanel.add(homePanel, BorderLayout.CENTER); // 默认显示主页

        contentPanel.add(mainContentPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(200, 0));
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

        JLabel userIconLabel = new JLabel("👨‍🏫");
        userIconLabel.setFont(new Font("微软雅黑", Font.PLAIN, 28));
        avatarCircle.add(userIconLabel);

        avatarPanel.add(avatarCircle);
        userPanel.add(avatarPanel, BorderLayout.WEST);

        // 右侧用户信息
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(new Color(245, 250, 255));

        // 用户名
        JLabel userNameLabel = new JLabel(teacher.getRealName());
        userNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        userNameLabel.setForeground(new Color(34, 34, 34));
        userNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 角色标签
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rolePanel.setBackground(new Color(245, 250, 255));
        rolePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel("教师");
        roleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setBackground(new Color(231, 76, 60));
        roleLabel.setOpaque(true);
        roleLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        rolePanel.add(roleLabel);

        // ID信息
        JLabel idLabel = new JLabel("ID: " + teacher.getUserId());
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
        separator.setMaximumSize(new Dimension(200, 1));
        separator.setForeground(new Color(240, 240, 240));
        sidebar.add(separator);

        // 导航菜单
        String[][] menuConfig = {
                {"home", "我的主页"},
                {"question", "题库管理"},
                {"paper", "试卷管理"},
                {"import", "导入题目"},
                {"student", "学生管理"}
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
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 10));
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
            case "question":
                return IconUtil.createDocumentIcon(color, size);
            case "paper":
                return IconUtil.createChartIcon(color, size);
            case "import":
                return IconUtil.createUploadIcon(color, size);
            case "student":
                return IconUtil.createUserIcon(color, size);
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
        String[] views = {"home", "question", "paper", "import", "student"};
        for (int i = 0; i < menuButtons.size(); i++) {
            JButton button = menuButtons.get(i);
            boolean isActive = i == getViewIndex(view);
            updateButtonStyle(button, isActive);
            updateButtonIcon(button, views[i], isActive);
        }

        // 切换内容 - 使用懒加载
        mainContentPanel.removeAll();

        switch (view) {
            case "home":
                if (homePanel == null) {
                    homePanel = new TeacherHomePanel(this, teacher);
                }
                mainContentPanel.add(homePanel, BorderLayout.CENTER);
                break;
            case "question":
                if (questionPanel == null) {
                    questionPanel = new TeacherQuestionPanel(questionService, new TeacherQuestionPanel.TeacherQuestionCallback() {
                        @Override
                        public void onAddQuestion() {
                            questionManager.showAddQuestionDialog();
                        }
                        @Override
                        public void onEditQuestion(int row) {
                            editQuestionAtRow(row);
                        }
                        @Override
                        public void onDeleteQuestion(int row) {
                            deleteQuestionAtRow(row);
                        }
                    });
                }
                mainContentPanel.add(questionPanel, BorderLayout.CENTER);
                break;
            case "paper":
                if (paperPanel == null) {
                    paperPanel = new TeacherPaperPanel(paperService, new TeacherPaperPanel.TeacherPaperCallback() {
                        @Override
                        public void onViewPaper(int row) {
                            viewPaperAtRow(row);
                        }
                        @Override
                        public void onEditPaper(int row) {
                            editPaperAtRow(row);
                        }
                        @Override
                        public void onDeletePaper(int row) {
                            deletePaperAtRow(row);
                        }
                        @Override
                        public void onTogglePublish(int row) {
                            togglePublishAtRow(row);
                        }
                    });
                }
                mainContentPanel.add(paperPanel, BorderLayout.CENTER);
                break;
            case "import":
                if (importPanel == null) {
                    importPanel = new TeacherImportPanel(questionService, this, teacher.getUserId(), new TeacherImportPanel.TeacherImportCallback() {
                        @Override
                        public void onImportSuccess() {
                            System.out.println("DEBUG: onImportSuccess called");
                            // 刷新题库管理面板数据
                            if (questionPanel != null) {
                                System.out.println("DEBUG: Refreshing question panel");
                                questionPanel.refreshData();
                            }
                            // 刷新试卷管理面板数据（因为导入可能影响试卷）
                            if (paperPanel != null) {
                                System.out.println("DEBUG: Refreshing paper panel");
                                paperPanel.refreshData();
                            } else {
                                System.out.println("DEBUG: paperPanel is null, but still need to ensure data is updated");
                                // 即使paperPanel未初始化，也要确保试卷数据在需要时被更新
                                // 我们调用mainFrame的refreshPaperData方法，该方法会处理未初始化的情况
                                refreshPaperData();
                            }
                        }

                        @Override
                        public void onCreatePaperWithQuestions(List<Question> questions) {
                            importManager.importAndGeneratePaper(questions);
                        }
                    });
                }
                mainContentPanel.add(importPanel, BorderLayout.CENTER);
                break;
            case "student":
                if (studentPanel == null) {
                    studentPanel = new TeacherStudentPanel(this);
                }
                mainContentPanel.add(studentPanel, BorderLayout.CENTER);
                break;
            default:
                if (homePanel == null) {
                    homePanel = new TeacherHomePanel(this, teacher);
                }
                mainContentPanel.add(homePanel, BorderLayout.CENTER);
        }

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private int getViewIndex(String view) {
        switch (view) {
            case "home": return 0;
            case "question": return 1;
            case "paper": return 2;
            case "import": return 3;
            case "student": return 4;
            default: return -1;
        }
    }





    /**
     * 编辑指定行的题目
     */
    private void editQuestionAtRow(int row) {
        try {
            List<Question> questions = questionService.getAllQuestions();
            if (row >= 0 && row < questions.size()) {
                Question question = questions.get(row);
                questionManager.showEditQuestionDialog(question);
            }
        } catch (Exception e) {
            UIUtil.showError(this, "编辑题目失败：" + e.getMessage());
        }
    }










    private void logout() {
        if (UIUtil.showConfirm(this, "确定要退出登录吗?")) {
            dispose();
            new TeacherLoginFrame().setVisible(true);
        }
    }




    /**
     * 删除指定行的题目
     */
    private void deleteQuestionAtRow(int row) {
        try {
            List<Question> questions = questionService.getAllQuestions();
            if (row >= 0 && row < questions.size()) {
                Question question = questions.get(row);
                questionManager.deleteQuestion(question);
            } else {
                UIUtil.showError(this, "无法找到对应的题目");
            }
        } catch (Exception e) {
            UIUtil.showError(this, "删除题目失败：" + e.getMessage());
        }
    }



    /**
     * 编辑指定行的试卷
     */
    private void editPaperAtRow(int row) {
        try {
            // 从paperPanel获取表格数据
            DefaultTableModel tableModel = paperPanel.getTableModel();
            String paperName = (String) tableModel.getValueAt(row, 0);

            Paper paper = paperService.getPaperByName(paperName);
            if (paper == null) {
                UIUtil.showError(this, "无法找到对应的试卷");
                return;
            }

            paperManager.showEditPaperDialog(paper);
        } catch (Exception e) {
            UIUtil.showError(this, "加载试卷信息失败：" + e.getMessage());
        }
    }



    /**
     * 查看指定行试卷的详细信息
     */
    private void viewPaperAtRow(int row) {
        try {
            // 从paperPanel获取表格数据
            DefaultTableModel tableModel = paperPanel.getTableModel();
            String paperName = (String) tableModel.getValueAt(row, 0);

            Paper paper = paperService.getPaperByName(paperName);
            if (paper == null) {
                UIUtil.showError(this, "无法找到对应的试卷");
                return;
            }

            paperManager.showPaperDetailDialog(paper);
        } catch (Exception e) {
            UIUtil.showError(this, "加载试卷信息失败：" + e.getMessage());
        }
    }





    /**
     * 删除指定行的试卷
     */
    private void deletePaperAtRow(int row) {
        try {
            // 从paperPanel获取表格数据
            DefaultTableModel tableModel = paperPanel.getTableModel();
            String paperName = (String) tableModel.getValueAt(row, 0);
            Paper paper = paperService.getPaperByName(paperName);

            if (paper == null) {
                UIUtil.showError(this, "无法找到对应的试卷");
                return;
            }

            paperManager.deletePaper(paper);
        } catch (Exception e) {
            UIUtil.showError(this, "删除试卷失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 切换试卷发布状态
     */
    private void togglePublishAtRow(int row) {
        try {
            // 从paperPanel获取表格数据
            DefaultTableModel tableModel = paperPanel.getTableModel();
            String paperName = (String) tableModel.getValueAt(row, 0);
            
            Paper paper = paperService.getPaperByName(paperName);
            if (paper == null) {
                UIUtil.showError(this, "无法找到对应的试卷");
                return;
            }

            paperManager.togglePublishStatus(paper);
        } catch (Exception e) {
            UIUtil.showError(this, "切换发布状态失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}