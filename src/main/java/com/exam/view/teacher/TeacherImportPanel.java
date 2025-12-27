package com.exam.view.teacher;

import com.exam.model.Question;
import com.exam.service.QuestionService;
import com.exam.util.QuestionImportUtil;
import com.exam.util.UIUtil;

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
    
    // 存储选择的文件
    private File selectedImportFile = null;
    
    // 回调接口
    public interface TeacherImportCallback {
        void onImportSuccess();
    }
    
    public TeacherImportPanel(QuestionService questionService, TeacherImportCallback callback) {
        this.questionService = questionService;
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
        gbc.weighty = 0.25;
        centerPanel.add(fileSelectionPanel, gbc);

        // 2. 格式说明区域
        JPanel formatPanel = createFormatDescriptionPanel();
        gbc.gridy = 1;
        gbc.weighty = 0.6;
        centerPanel.add(formatPanel, gbc);

        // 3. 操作按钮区域
        JPanel actionPanel = createImportActionPanel();
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

        // 所有内容放在一行：文件图标 + 文件信息 + 按钮
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        contentPanel.setBackground(Color.WHITE);

        // 文件图标
        JLabel fileIconLabel = new JLabel("📄");
        fileIconLabel.setFont(new Font("微软雅黑", Font.PLAIN, 32));
        contentPanel.add(fileIconLabel);

        // 文件信息面板（包含关闭按钮）
        JPanel fileInfoWrapper = new JPanel(new BorderLayout(5, 0));
        fileInfoWrapper.setBackground(Color.WHITE);

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
        contentPanel.add(fileInfoWrapper);

        // 添加一些水平间隙
        contentPanel.add(Box.createHorizontalStrut(20));

        // 选择文件按钮
        JButton selectFileButton = TeacherUIHelper.createStyledButton("选择文件", UIUtil.PRIMARY_COLOR);
        selectFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择题目文件");
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
        contentPanel.add(selectFileButton);

        // 开始导入按钮
        JButton importButton = TeacherUIHelper.createStyledButton("开始导入", UIUtil.SUCCESS_COLOR);
        importButton.addActionListener(e -> startImport());
        contentPanel.add(importButton);

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
                BorderFactory.createEmptyBorder(15, 20, 20, 20)
        ));

        // 格式说明文本
        JTextArea formatText = new JTextArea();
        formatText.setEditable(false);
        formatText.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        formatText.setForeground(new Color(80, 80, 80));
        formatText.setBackground(new Color(248, 250, 252));
        formatText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formatText.setLineWrap(true);
        formatText.setWrapStyleWord(true);

        String formatInfo = "文件格式：题目类型|科目|题目内容|选项A|选项B|选项C|选项D|正确答案|分值|难度|解析\n\n"
                + "• 题目类型：SINGLE(单选)、MULTIPLE(多选)、JUDGE(判断)、BLANK(填空)\n"
                + "• 难度：EASY(简单)、MEDIUM(中等)、HARD(困难)\n"
                + "• 以#开头的行为注释，会被忽略\n\n"
                + "示例：\n"
                + "SINGLE|Java|Java中哪个关键字用于定义常量？|const|final|static|constant|B|5|EASY|解析内容";

        formatText.setText(formatInfo);

        JScrollPane scrollPane = new JScrollPane(formatText);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.setPreferredSize(new Dimension(0, 120));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
    
    /**
     * 创建导入操作面板
     */
    private JPanel createImportActionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 220, 240), 2),
                        "导入操作",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("微软雅黑", Font.BOLD, 14),
                        UIUtil.PRIMARY_COLOR
                ),
                BorderFactory.createEmptyBorder(10, 20, 15, 20)
        ));

        // 主内容面板：使用BorderLayout将提示和按钮放在同一行
        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setBackground(Color.WHITE);

        // 左侧：提示信息
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        infoPanel.setBackground(new Color(255, 248, 225));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 193, 7)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel infoIcon = new JLabel("ℹ️");
        infoIcon.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JLabel infoLabel = new JLabel("导入前请确保文件格式正确，可先下载模板参考");
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(102, 77, 3));

        infoPanel.add(infoIcon);
        infoPanel.add(infoLabel);

        contentPanel.add(infoPanel, BorderLayout.CENTER);

        // 右侧：按钮区域（只保留下载模板按钮）
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton downloadTemplateButton = TeacherUIHelper.createStyledButton("下载模板文件", new Color(52, 152, 219));
        downloadTemplateButton.addActionListener(e -> downloadTemplate());

        buttonPanel.add(downloadTemplateButton);

        contentPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }
    
    /**
     * 下载模板文件
     */
    private void downloadTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存模板文件");
        fileChooser.setSelectedFile(new File("题目导入模板.txt"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("文本文件 (*.txt)", "txt");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // 确保文件扩展名为.txt
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }

            try {
                QuestionImportUtil.generateTemplate(file);
                UIUtil.showInfo(this, "模板文件已保存到：\n" + file.getAbsolutePath());
            } catch (Exception e) {
                UIUtil.showError(this, "保存模板文件失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 开始导入
     */
    private void startImport() {
        if (selectedImportFile == null) {
            UIUtil.showWarning(this, "请先选择要导入的文件");
            return;
        }

        if (!selectedImportFile.exists()) {
            UIUtil.showError(this, "文件不存在，请重新选择");
            selectedImportFile = null;
            return;
        }

        importQuestionsFromFile(selectedImportFile);
    }
    
    /**
     * 从文件导入题目
     */
    private void importQuestionsFromFile(File file) {
        try {
            // 读取题目
            List<Question> questions = QuestionImportUtil.importFromTextFile(file, null);
            
            // 批量添加题目
            questionService.batchAddQuestions(questions);
            
            int importedCount = questions.size();
            UIUtil.showInfo(this, "成功导入 " + importedCount + " 道题目！");
            
            // 清除选择的文件
            selectedImportFile = null;
            
            // 通知主框架刷新数据
            if (callback != null) {
                callback.onImportSuccess();
            }
        } catch (Exception e) {
            UIUtil.showError(this, "导入失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
