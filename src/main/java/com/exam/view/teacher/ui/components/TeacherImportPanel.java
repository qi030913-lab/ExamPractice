package com.exam.view.teacher.ui.components;

import com.exam.model.Question;
import com.exam.service.QuestionService;
import com.exam.util.QuestionImportUtil;
import com.exam.util.UIUtil;
import com.exam.view.teacher.TeacherUIHelper;
import com.exam.view.teacher.manager.ImportManager;
import com.exam.view.teacher.TeacherMainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * 教师端 - 导入题目面板
 */
public class TeacherImportPanel extends JPanel {
    private final TeacherImportCallback callback;
    private final QuestionService questionService;
    private final TeacherMainFrame mainFrame;  // 添加mainFrame引用
    private final int userId; // 用户ID
    
    // 存储选择的文件
    private File selectedImportFile = null;
    
    // 回调接口
    public interface TeacherImportCallback {
        void onImportSuccess();
        void onCreatePaperWithQuestions(List<Question> questions);
    }
    
    public TeacherImportPanel(QuestionService questionService, TeacherMainFrame mainFrame, int userId, TeacherImportCallback callback) {
        this.questionService = questionService;
        this.mainFrame = mainFrame;
        this.userId = userId;
        this.callback = callback;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        // 主内容区
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 标题区域
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("导入题目");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(UIUtil.TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        contentPanel.add(titlePanel, BorderLayout.NORTH);

        // 中心内容区
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // 创建三个主要区域
        // 1. 文件选择区域
        JPanel fileSelectionPanel = createFileSelectionPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.005;  // 从0.01减小到0.005
        centerPanel.add(fileSelectionPanel, gbc);

        // 2. 格式说明区域
        JPanel formatPanel = createFormatDescriptionPanel();
        gbc.gridy = 1;
        gbc.weighty = 0.845;  // 从0.84增大到0.845
        centerPanel.add(formatPanel, gbc);

        // 3. 导入说明区域
        JPanel actionPanel = createImportInstructionPanel();
        gbc.gridy = 2;
        gbc.weighty = 0.15;
        centerPanel.add(actionPanel, gbc);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    /**
     * 创建文件选择面板
     */
    private JPanel createFileSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 220, 240), 2),
                        "选择导入文件",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("微软雅黑", Font.BOLD, 14),
                        UIUtil.PRIMARY_COLOR
                ),
                BorderFactory.createEmptyBorder(10, 20, 15, 20)
        ));

        // 使用BorderLayout确保所有元素可见
        JPanel contentPanel = new JPanel(new BorderLayout(15, 10));
        contentPanel.setBackground(Color.WHITE);

        // 左侧：文件信息区域
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(Color.WHITE);

        // 文件图标
        JLabel fileIconLabel = new JLabel("📄");
        fileIconLabel.setFont(new Font("微软雅黑", Font.PLAIN, 32));
        leftPanel.add(fileIconLabel);

        // 文件信息面板（包含关闭按钮）
        JPanel fileInfoWrapper = new JPanel(new BorderLayout(5, 0));
        fileInfoWrapper.setBackground(Color.WHITE);
        fileInfoWrapper.setPreferredSize(new Dimension(400, 50));

        JPanel fileDetailsPanel = new JPanel();
        fileDetailsPanel.setLayout(new BoxLayout(fileDetailsPanel, BoxLayout.Y_AXIS));
        fileDetailsPanel.setBackground(Color.WHITE);

        JLabel fileNameLabel = new JLabel("未选择文件");
        fileNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        fileNameLabel.setForeground(new Color(100, 100, 100));
        fileNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel filePathLabel = new JLabel("请点击右侧按钮选择题目文件（.txt格式）");
        filePathLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        filePathLabel.setForeground(new Color(120, 120, 120));
        filePathLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        fileDetailsPanel.add(fileNameLabel);
        fileDetailsPanel.add(Box.createVerticalStrut(3));
        fileDetailsPanel.add(filePathLabel);

        fileInfoWrapper.add(fileDetailsPanel, BorderLayout.CENTER);

        // 关闭按钮（右上角的×）
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
        closeButton.setForeground(new Color(150, 150, 150));
        closeButton.setBackground(Color.WHITE);
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setVisible(false); // 初始隐藏
        closeButton.setPreferredSize(new Dimension(25, 25));

        // 鼠标悬停效果
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(UIUtil.DANGER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(new Color(150, 150, 150));
            }
        });

        // 清除文件选择
        closeButton.addActionListener(e -> {
            selectedImportFile = null;
            fileNameLabel.setText("未选择文件");
            fileNameLabel.setForeground(new Color(100, 100, 100));
            filePathLabel.setText("请点击右侧按钮选择题目文件（.txt格式）");
            closeButton.setVisible(false);
        });

        fileInfoWrapper.add(closeButton, BorderLayout.EAST);
        leftPanel.add(fileInfoWrapper);
        contentPanel.add(leftPanel, BorderLayout.CENTER);

        // 右侧：按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        // 选择文件按钮
        JButton selectFileButton = TeacherUIHelper.createStyledButton("选择文件", UIUtil.PRIMARY_COLOR);
        selectFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择题目文件");
            fileChooser.setPreferredSize(new Dimension(600, 400)); // 设置文件选择器弹窗大小
            FileNameExtensionFilter filter = new FileNameExtensionFilter("文本文件 (*.txt)", "txt");
            fileChooser.setFileFilter(filter);

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImportFile = fileChooser.getSelectedFile();
                fileNameLabel.setText(selectedImportFile.getName());
                fileNameLabel.setForeground(UIUtil.PRIMARY_COLOR);
                filePathLabel.setText(selectedImportFile.getAbsolutePath());
                closeButton.setVisible(true); // 显示关闭按钮
            }
        });
        buttonPanel.add(selectFileButton);

        // 开始导入按钮
        JButton importButton = TeacherUIHelper.createStyledButton("开始导入", UIUtil.SUCCESS_COLOR);
        importButton.addActionListener(e -> startImport());
        buttonPanel.add(importButton);
        
        contentPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * 创建格式说明面板
     */
    private JPanel createFormatDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 220, 240), 2),
                        "文件格式说明",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("微软雅黑", Font.BOLD, 14),
                        UIUtil.PRIMARY_COLOR
                ),
                BorderFactory.createEmptyBorder(10, 20, 15, 20)
        ));

        // 格式说明文本
        JTextArea formatText = new JTextArea();
        formatText.setText(
                "文件格式要求：\n\n" +
                "1. 每道题目占一行，格式为：\n" +
                "   题目内容|选项A|选项B|选项C|选项D|正确答案|题目类型|难度|分值\n\n" +
                "2. 题目类型：SINGLE(单选)、MULTIPLE(多选)、JUDGE(判断)、BLANK(填空)、APPLICATION(应用题)、ALGORITHM(算法设计题)、SHORT_ANSWER(简答题)、COMPREHENSIVE(综合题)、ESSAY(论述题)、MATERIAL_ANALYSIS(材料分析题)\n" +
                "3. 难度：EASY(简单)、MEDIUM(中等)、HARD(困难)\n\n" +
                "示例：\n" +
                "   Java是面向对象语言|是|否|不确定||A|JUDGE|MEDIUM|5\n" +
                "   请编写Java程序实现链表反转|||||1.定义链表结构 2.递归实现|APPLICATION|HARD|20\n" +
                "   请简述面向对象的三大特征|||||1.封装 2.继承 3.多态|SHORT_ANSWER|MEDIUM|15\n" +
                "   请论述人工智能的发展趋势|||||1.技术现状 2.未来趋势 3.社会影响|ESSAY|HARD|25\n" +
                "   阅读材料并分析修辞手法|||||1.拟人手法 2.营造意境|MATERIAL_ANALYSIS|MEDIUM|20"
        );
        formatText.setFont(new Font("微软雅黑", Font.PLAIN, 14));  // 字体从12改为14
        formatText.setEditable(false);
        formatText.setLineWrap(true);
        formatText.setWrapStyleWord(true);
        formatText.setBackground(new Color(248, 250, 252));

        JScrollPane scrollPane = new JScrollPane(formatText);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
    
    /**
     * 创建导入说明面板
     */
    private JPanel createImportInstructionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 220, 240), 2),
                        "导入说明",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("微软雅黑", Font.BOLD, 14),
                        UIUtil.PRIMARY_COLOR
                ),
                BorderFactory.createEmptyBorder(10, 20, 15, 20)
        ));

        // 创建一个面板，包含提醒文字和下载按钮
        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.setBackground(Color.WHITE);

        // 提醒文字
        JLabel instructionLabel = new JLabel("请先下载模板文件，按照模板格式编辑题目后导入");
        instructionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        instructionLabel.setForeground(new Color(255, 165, 0)); // 橙色文字
        instructionLabel.setBackground(new Color(230, 240, 250)); // 淡蓝色背景
        instructionLabel.setOpaque(true); // 使背景颜色生效
        instructionLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 200), 1)); // 蓝色边框
        instructionLabel.setHorizontalAlignment(SwingConstants.LEFT); // 左对齐
        instructionLabel.setPreferredSize(new Dimension(350, 30)); // 设置固定大小

        // 下载模板按钮
        JButton downloadTemplateButton = TeacherUIHelper.createStyledButton("下载模板", UIUtil.PRIMARY_COLOR);
        downloadTemplateButton.addActionListener(e -> downloadTemplate());

        contentPanel.add(instructionLabel, BorderLayout.CENTER);
        contentPanel.add(downloadTemplateButton, BorderLayout.EAST);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }
    
    /**
     * 下载模板文件
     */
    private void downloadTemplate() {
        // 创建模板内容
        String templateContent = 
            "题目内容|选项A|选项B|选项C|选项D|正确答案|题目类型|难度|分值\n" +
            "Java是面向对象语言|是|否|不确定||A|JUDGE|MEDIUM|5\n" +
            "Java中的继承关键字是|extends|implements|abstract|interface|A|SINGLE|MEDIUM|5\n" +
            "请编写一个Java程序，实现学生成绩管理系统|||||1.定义Student类 2.使用ArrayList存储 3.实现CRUD操作|APPLICATION|MEDIUM|20\n" +
            "请设计一个快速排序算法并分析时间复杂度|||||1.选择基准元素 2.分区操作 3.递归排序 4.时间复杂度O(nlogn)|ALGORITHM|HARD|25\n" +
            "请简述Java中面向对象的三大特征及其含义|||||1.封装 2.继承 3.多态|SHORT_ANSWER|MEDIUM|15\n" +
            "请设计并实现一个学生管理系统，包括学生信息管理、成绩管理等功能|||||1.系统架构设计 2.数据库设计 3.类图设计|COMPREHENSIVE|HARD|30\n" +
            "请论述人工智能技术的发展现状和未来趋势，分析其对社会的影响|||||1.技术发展现状 2.未来趋势 3.社会影响|ESSAY|HARD|25\n" +
            "阅读材料并分析修辞手法和表达效果|||||1.拟人手法 2.营造意境|MATERIAL_ANALYSIS|MEDIUM|20\n" +
            "请在此处添加您的题目内容|选项A|选项B|选项C|选项D|A|SINGLE|EASY|5\n";

        try {
            // 创建文件选择器来选择保存位置
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("保存模板文件");
            fileChooser.setSelectedFile(new java.io.File("题目导入模板.txt"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("文本文件 (*.txt)", "txt"));

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                
                // 确保文件有.txt扩展名
                if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                    selectedFile = new java.io.File(selectedFile.getAbsolutePath() + ".txt");
                }

                // 写入模板内容
                try (java.io.FileWriter writer = new java.io.FileWriter(selectedFile)) {
                    writer.write(templateContent);
                }

                UIUtil.showInfo(this, "模板文件已保存到：" + selectedFile.getAbsolutePath());
            }
        } catch (Exception e) {
            UIUtil.showError(this, "保存模板文件失败：" + e.getMessage());
        }
    }
    
    /**
     * 开始导入题目
     */
    private void startImport() {
        if (selectedImportFile == null) {
            UIUtil.showWarning(this, "请先选择要导入的文件");
            return;
        }

        try {
            List<Question> questions = QuestionImportUtil.importQuestionsFromFile(selectedImportFile.getAbsolutePath());
            if (questions.isEmpty()) {
                UIUtil.showWarning(this, "文件中没有找到有效的题目数据");
                return;
            }

            // 显示选项对话框，让用户选择导入方式
            Object[] options = {"仅导入题目", "导入并生成试卷", "取消"};
            int result = JOptionPane.showOptionDialog(
                    this,
                    "选择导入方式：\n\n共找到 " + questions.size() + " 道题目",
                    "导入方式选择",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (result == JOptionPane.YES_OPTION) { // 仅导入题目
                ImportManager importManager = new ImportManager(questionService, mainFrame);
                importManager.importQuestions(questions, callback);
            } else if (result == JOptionPane.NO_OPTION) { // 导入并生成试卷
                if (callback != null) {
                    callback.onCreatePaperWithQuestions(questions);
                }
            }
            // 如果是取消或关闭对话框，则不执行任何操作

        } catch (Exception e) {
            UIUtil.showError(this, "导入失败：" + e.getMessage());
        }
    }
    
    /**
     * 导入题目并自动生成试卷
     */
    private void importAndGeneratePaper() {
        if (selectedImportFile == null) {
            UIUtil.showWarning(this, "请先选择要导入的文件");
            return;
        }

        try {
            List<Question> questions = QuestionImportUtil.importQuestionsFromFile(selectedImportFile.getAbsolutePath());
            if (questions.isEmpty()) {
                UIUtil.showWarning(this, "文件中没有找到有效的题目数据");
                return;
            }

            if (callback != null) {
                callback.onCreatePaperWithQuestions(questions);
            }
        } catch (Exception e) {
            UIUtil.showError(this, "导入失败：" + e.getMessage());
        }
    }
}
