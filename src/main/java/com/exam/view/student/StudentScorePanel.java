package com.exam.view.student;

import com.exam.model.User;
import com.exam.model.ExamRecord;
import com.exam.model.AnswerRecord;
import com.exam.service.ExamService;
import com.exam.util.UIUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.List;

/**
 * 学生端 - 成绩查询面板
 * 
 * 功能说明：
 * 1. 展示学生的所有考试记录
 * 2. 显示详细的成绩信息（总分、得分、正确题数、错误题数、耗时等）
 * 3. 查看考试详情（包括错题分析）
 * 
 * @author 系统管理员
 * @version 1.0
 */
public class StudentScorePanel extends JPanel {
    private final User student;
    private final ExamService examService;
    private DefaultTableModel scoreTableModel;

    public StudentScorePanel(User student) {
        this.student = student;
        this.examService = new ExamService();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 标题区域
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);

        // 成绩记录表格区域
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // 加载成绩数据
        loadScores();
    }

    /**
     * 创建标题面板
     */
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel titleLabel = new JLabel("成绩查询");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(UIUtil.TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        return titlePanel;
    }

    /**
     * 创建表格面板
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
        
        // 成绩表格
        String[] columns = {"试卷名称", "总分", "得分", "正确题数", "错误题数", "考试时间", "耗时", "详情"};
        scoreTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
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
        
        return tablePanel;
    }

    /**
     * 加载成绩数据
     */
    private void loadScores() {
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
                Object[] row = {"暂无考试记录", "", "", "", "", "", "", ""};
                scoreTableModel.addRow(row);
            }
        } catch (Exception e) {
            UIUtil.showError(this, "加载成绩失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 显示考试详情对话框
     */
    private void showExamDetail(int recordId) {
        try {
            ExamRecord record = examService.getExamRecordById(recordId);
            if (record == null) {
                UIUtil.showError(this, "找不到考试记录");
                return;
            }
            
            List<AnswerRecord> answerRecords = examService.getAnswerRecords(recordId);
            
            // 创建对话框
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "考试详情", true);
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
            
            // 填充答题数据
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

    // ========== 内部类：成绩详情按钮渲染器和编辑器 ==========

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
