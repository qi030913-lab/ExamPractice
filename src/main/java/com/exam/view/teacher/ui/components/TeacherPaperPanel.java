package com.exam.view.teacher.ui.components;

import com.exam.model.Paper;
import com.exam.service.PaperService;
import com.exam.util.UIUtil;
import com.exam.view.teacher.TeacherUIHelper;
import com.exam.view.teacher.manager.PaperManager;
import com.exam.view.teacher.ui.components.PaperButtonEditor;
import com.exam.view.teacher.ui.components.PaperButtonRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 教师端 - 试卷管理面板
 */
public class TeacherPaperPanel extends JPanel {
    private final TeacherPaperCallback callback;
    private final PaperService paperService;
    
    private JTable paperManagementTable;
    private DefaultTableModel paperTableModel;
    
    // 回调接口
    public interface TeacherPaperCallback {
        void onAddPaper();
        void onViewPaper(int row);
        void onEditPaper(int row);
        void onDeletePaper(int row);
        void onTogglePublish(int row);
    }
    
    public TeacherPaperPanel(PaperService paperService, TeacherPaperCallback callback) {
        this.paperService = paperService;
        this.callback = callback;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        // 右侧主内容区
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // 标题区域
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        JLabel titleLabel = new JLabel("试卷管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(UIUtil.TEXT_COLOR);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        // 添加试卷按钮放在右侧
        JButton addPaperButton = TeacherUIHelper.createStyledButton("创建试卷", UIUtil.SUCCESS_COLOR);
        addPaperButton.addActionListener(e -> {
            if (callback != null) {
                callback.onAddPaper();
            }
        });
        titlePanel.add(addPaperButton, BorderLayout.EAST);

        contentPanel.add(titlePanel, BorderLayout.NORTH);

        // 表格面板
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));

        // 表格
        String[] columns = {"试卷名称", "科目", "题目数", "总分", "时长(分钟)", "状态", "操作"};
        paperTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 操作列可编辑
                return column == 6;
            }
        };
        paperManagementTable = new JTable(paperTableModel) {
            @Override
            public Class<?> getColumnClass(int column) {
                // 操作列使用JPanel类型
                if (column == 6) {
                    return JPanel.class;
                }
                return String.class;
            }
        };
        paperManagementTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        paperManagementTable.setRowHeight(50);
        paperManagementTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paperManagementTable.setGridColor(new Color(230, 230, 230));
        paperManagementTable.setShowGrid(true);
        paperManagementTable.setSelectionBackground(new Color(232, 240, 254));
        paperManagementTable.setSelectionForeground(UIUtil.TEXT_COLOR);

        // 设置操作列渲染器
        paperManagementTable.getColumnModel().getColumn(6).setCellRenderer(new PaperButtonRenderer());
        paperManagementTable.getColumnModel().getColumn(6).setCellEditor(new PaperButtonEditor(
                paperManagementTable, paperTableModel, paperService, new PaperButtonEditor.PaperButtonCallback() {
            @Override
            public void onView(int row) {
                if (callback != null) {
                    callback.onViewPaper(row);
                }
            }

            @Override
            public void onEdit(int row) {
                if (callback != null) {
                    callback.onEditPaper(row);
                }
            }

            @Override
            public void onDelete(int row) {
                if (callback != null) {
                    callback.onDeletePaper(row);
                }
            }

            @Override
            public void onTogglePublish(int row) {
                if (callback != null) {
                    callback.onTogglePublish(row);
                }
            }
        }));
        // 设置操作列宽度
        paperManagementTable.getColumnModel().getColumn(6).setPreferredWidth(180);
        paperManagementTable.getColumnModel().getColumn(6).setMinWidth(180);

        // 表头样式
        paperManagementTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        paperManagementTable.getTableHeader().setBackground(new Color(245, 247, 250));
        paperManagementTable.getTableHeader().setForeground(UIUtil.TEXT_COLOR);
        paperManagementTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        paperManagementTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        JScrollPane scrollPane = new JScrollPane(paperManagementTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // 加载数据
        loadPapersData();
    }
    
    /**
     * 加载试卷数据
     */
    private void loadPapersData() {
        paperTableModel.setRowCount(0);
        try {
            List<Paper> papers = paperService.getAllPapers();
            for (Paper paper : papers) {
                int questionCount = paper.getQuestions() != null ? paper.getQuestions().size() : 0;
                String status = paper.getIsPublished() != null && paper.getIsPublished() ? "已发布" : "未发布";
                Color statusColor = paper.getIsPublished() != null && paper.getIsPublished() ? new Color(46, 125, 50) : new Color(211, 47, 47); // 绿色表示已发布，红色表示未发布
                String statusDisplay = "<html><span style='color: rgb(" + statusColor.getRed() + "," + statusColor.getGreen() + "," + statusColor.getBlue() + "); font-weight: bold;'>" + status + "</span></html>";
                Object[] row = {
                        paper.getPaperName(),
                        paper.getSubject(),
                        questionCount,
                        paper.getTotalScore(),
                        paper.getDuration(),
                        statusDisplay,
                        "" // 操作列，由渲染器处理
                };
                paperTableModel.addRow(row);
            }
        } catch (Exception e) {
            UIUtil.showError(this, "加载试卷失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 刷新数据
     */
    public void refreshData() {
        loadPapersData();
    }
    
    /**
     * 获取表格模型（供主框架使用）
     */
    public DefaultTableModel getTableModel() {
        return paperTableModel;
    }
}