package com.exam.view.teacher.manager;

import com.exam.model.Paper;
import com.exam.model.Question;
import com.exam.service.PaperService;
import com.exam.util.UIUtil;
import com.exam.view.teacher.TeacherMainFrame;
import com.exam.view.teacher.ui.components.PaperButtonEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 试卷管理器 - 处理所有与试卷相关的操作
 */
public class PaperManager {
    private final PaperService paperService;
    private final TeacherMainFrame mainFrame;
    
    public PaperManager(PaperService paperService, TeacherMainFrame mainFrame) {
        this.paperService = paperService;
        this.mainFrame = mainFrame;
    }
    
    /**
     * 显示编辑试卷对话框
     */
    public void showEditPaperDialog(Paper paper) {
        JDialog dialog = new JDialog(mainFrame, "编辑试卷", true);
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(new Color(245, 250, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 标题
        JLabel titleLabel = new JLabel("编辑试卷");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("试卷信息"));

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 试卷名称
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("试卷名称："), gbc);
        JTextField paperNameField = new JTextField(paper.getPaperName(), 20);
        gbc.gridx = 1;
        formPanel.add(paperNameField, gbc);

        // 科目
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("科目："), gbc);
        String[] subjects = {"Java", "Vue", "数据结构", "马克思主义", "计算机网络", "操作系统", "数据库", "其他"};
        JComboBox<String> subjectBox = new JComboBox<>(subjects);
        subjectBox.setSelectedItem(paper.getSubject());
        gbc.gridx = 1;
        formPanel.add(subjectBox, gbc);

        // 及格分数
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("及格分数："), gbc);
        JSpinner passScoreSpinner = new JSpinner(new javax.swing.SpinnerNumberModel((int)paper.getPassScore().intValue(), (int)0, (int)100, (int)1));
        gbc.gridx = 1;
        formPanel.add(passScoreSpinner, gbc);

        // 考试时长
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("考试时长(分钟)："), gbc);
        JSpinner durationSpinner = new JSpinner(new javax.swing.SpinnerNumberModel((int)paper.getDuration().intValue(), (int)1, (int)300, (int)5));
        gbc.gridx = 1;
        formPanel.add(durationSpinner, gbc);

        // 描述
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("描述："), gbc);
        JTextArea descArea = new JTextArea(paper.getDescription(), 3, 20);
        JScrollPane descScroll = new JScrollPane(descArea);
        gbc.gridx = 1;
        formPanel.add(descScroll, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));

        JButton saveButton = new JButton("保存修改");
        saveButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        saveButton.setBackground(UIUtil.SUCCESS_COLOR);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        saveButton.addActionListener(e -> {
            String paperName = paperNameField.getText().trim();
            String subject = (String) subjectBox.getSelectedItem();
            int passScore = (Integer) passScoreSpinner.getValue();
            int duration = (Integer) durationSpinner.getValue();

            if (paperName.isEmpty()) {
                UIUtil.showWarning(dialog, "试卷名称不能为空");
                return;
            }
            if (subject.isEmpty()) {
                UIUtil.showWarning(dialog, "科目不能为空");
                return;
            }

            try {
                paper.setPaperName(paperName);
                paper.setSubject(subject);
                paper.setDuration(duration);
                paper.setPassScore(passScore);
                paper.setDescription(descArea.getText().trim());

                paperService.updatePaper(paper);

                UIUtil.showInfo(dialog, "试卷修改成功！");
                dialog.dispose();
                // 通知主框架刷新数据
                mainFrame.refreshPaperData();

            } catch (Exception ex) {
                UIUtil.showError(dialog, "修改试卷失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        });
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cancelButton.setBackground(new Color(120, 144, 156));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    /**
     * 显示试卷详细信息对话框
     */
    public void showPaperDetailDialog(Paper paper) {
        JDialog dialog = new JDialog(mainFrame, "试卷详情", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 标题
        JLabel titleLabel = new JLabel("试卷详细信息");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 试卷基本信息
        JPanel infoPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("基本信息"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        infoPanel.add(createInfoLabel("试卷名称："));
        infoPanel.add(createInfoValueLabel(paper.getPaperName()));

        infoPanel.add(createInfoLabel("科目："));
        infoPanel.add(createInfoValueLabel(paper.getSubject()));

        infoPanel.add(createInfoLabel("题目数量："));
        int questionCount = paper.getQuestions() != null ? paper.getQuestions().size() : 0;
        infoPanel.add(createInfoValueLabel(questionCount + " 道"));

        infoPanel.add(createInfoLabel("总分："));
        infoPanel.add(createInfoValueLabel(paper.getTotalScore() + " 分"));

        infoPanel.add(createInfoLabel("考试时长："));
        infoPanel.add(createInfoValueLabel(paper.getDuration() + " 分钟"));

        String status = paper.getIsPublished() != null && paper.getIsPublished() ? "已发布" : "未发布";
        Color statusColor = paper.getIsPublished() != null && paper.getIsPublished() ? new Color(46, 125, 50) : new Color(211, 47, 47); // 绿色表示已发布，红色表示未发布
        JLabel statusValueLabel = new JLabel(status);
        statusValueLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        statusValueLabel.setForeground(statusColor);
        infoPanel.add(createInfoLabel("发布状态："));
        infoPanel.add(statusValueLabel);

        // 题目列表
        JPanel questionsPanel = new JPanel(new BorderLayout(0, 10));
        questionsPanel.setBackground(Color.WHITE);
        questionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("题目列表"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        String[] columns = {"序号", "类型", "题目内容", "分值"};
        DefaultTableModel questionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (paper.getQuestions() != null) {
            int index = 1;
            for (Question q : paper.getQuestions()) {
                Object[] row = {
                        index++,
                        q.getQuestionType().getDescription(),
                        UIUtil.truncate(q.getContent(), 60),
                        q.getScore() + "分"
                };
                questionTableModel.addRow(row);
            }
        }

        JTable questionTable = new JTable(questionTableModel);
        questionTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        questionTable.setRowHeight(35);
        questionTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        questionTable.getTableHeader().setBackground(new Color(245, 247, 250));

        JScrollPane scrollPane = new JScrollPane(questionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        questionsPanel.add(scrollPane, BorderLayout.CENTER);

        // 组合面板
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(infoPanel, BorderLayout.NORTH);
        centerPanel.add(questionsPanel, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        JButton closeButton = new JButton("关闭");
        closeButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        closeButton.setBackground(UIUtil.PRIMARY_COLOR);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    /**
     * 删除试卷
     */
    public void deletePaper(Paper paper) {
        if (!UIUtil.showConfirm(mainFrame, "确定要删除这份试卷吗？\n删除后将无法恢复！")) {
            return;
        }

        try {
            // 删除试卷
            paperService.deletePaper(paper.getPaperId());

            UIUtil.showInfo(mainFrame, "删除成功");
            // 通知主框架刷新数据
            mainFrame.refreshPaperData();
        } catch (Exception e) {
            UIUtil.showError(mainFrame, "删除失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 切换试卷发布状态
     */
    public void togglePublishStatus(Paper paper) {
        String action = ""; // 声明在外部，以便在catch块中使用

        try {
            boolean currentStatus = paper.getIsPublished() != null && paper.getIsPublished();
            action = currentStatus ? "取消发布" : "发布";

            int confirm = JOptionPane.showConfirmDialog(
                    mainFrame,
                    "确定要" + action + "试卷《" + paper.getPaperName() + "》吗？",
                    action + "确认",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (currentStatus) {
                    paperService.unpublishPaper(paper.getPaperId());
                    UIUtil.showInfo(mainFrame, "试卷已取消发布");
                } else {
                    paperService.publishPaper(paper.getPaperId());
                    UIUtil.showInfo(mainFrame, "试卷已发布，学生端现在可以看到该试卷了");
                }
                // 通知主框架刷新数据
                mainFrame.refreshPaperData();
            }
        } catch (Exception e) {
            UIUtil.showError(mainFrame, action + "失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        label.setForeground(new Color(100, 100, 100));
        return label;
    }

    private JLabel createInfoValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.BOLD, 13));
        label.setForeground(UIUtil.TEXT_COLOR);
        return label;
    }
}