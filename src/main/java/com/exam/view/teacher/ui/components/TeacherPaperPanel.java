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
    private JScrollPane scrollPane;
    private JPanel tablePanel;
    
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
        tablePanel = new JPanel(new BorderLayout(0, 15));
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

        scrollPane = new JScrollPane(paperManagementTable);
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
            
            // 检查是否有数据，如果没有则显示提示
            updateTableDisplay();
        } catch (Exception e) {
            UIUtil.showError(this, "加载试卷失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateTableDisplay() {
        // 如果表格没有数据，显示"暂无试卷"提示
        if (paperTableModel.getRowCount() == 0) {
            showNoDataMessage();
        } else {
            // 显示表头
            paperManagementTable.getTableHeader().setVisible(true);
            // 确保显示表格
            showTable();
        }
    }
    
    private void showNoDataMessage() {
        // 隐藏表格组件
        paperManagementTable.setVisible(false);
        
        // 创建"暂无试卷"提示标签
        JLabel noDataLabel = new JLabel("暂无试卷", SwingConstants.CENTER);
        noDataLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        noDataLabel.setForeground(new Color(150, 150, 150));
        noDataLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // 获取表格所在的面板并替换为提示标签
        Container viewport = paperManagementTable.getParent();
        if (viewport == null) return;
        
        Container scrollPane = viewport.getParent();
        if (scrollPane == null) return;
        
        JPanel tablePanel = (JPanel) scrollPane.getParent();
        if (tablePanel == null) return;

        tablePanel.removeAll();
        tablePanel.setLayout(new BorderLayout(0, 15));
        tablePanel.add(noDataLabel, BorderLayout.CENTER);
        
        tablePanel.revalidate();
        tablePanel.repaint();
    }
    
    private void showTable() {
        // 确保表格可见
        paperManagementTable.setVisible(true);
        
        // 恢复表格显示
        Container viewport = paperManagementTable.getParent();
        if (viewport == null) return;
        
        Container scrollPane = viewport.getParent();
        if (scrollPane == null) return;
        
        JPanel tablePanel = (JPanel) scrollPane.getParent();
        if (tablePanel == null) return;
        
        // 检查当前是否显示的是提示标签，如果是则需要重新设置
        if (tablePanel.getComponentCount() == 0 || !(tablePanel.getComponent(0) instanceof JLabel)) {
            tablePanel.removeAll();
            tablePanel.setLayout(new BorderLayout(0, 15));
            tablePanel.add(scrollPane, BorderLayout.CENTER);
        }
        
        tablePanel.revalidate();
        tablePanel.repaint();
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