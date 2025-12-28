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
     * 刷新数据
     */
    public void refreshData() {
        System.out.println("DEBUG: TeacherPaperPanel.refreshData() called");
        loadPapersData();
    }
    
    /**
     * 加载试卷数据
     */
    private void loadPapersData() {
        System.out.println("DEBUG: TeacherPaperPanel.loadPapersData() called");
        paperTableModel.setRowCount(0);
        try {
            List<Paper> papers = paperService.getAllPapers();
            System.out.println("DEBUG: Loaded " + papers.size() + " papers from service");
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
            System.out.println("DEBUG: After updateTableDisplay, row count: " + paperTableModel.getRowCount());
        } catch (Exception e) {
            System.out.println("DEBUG: Error in loadPapersData: " + e.getMessage());
            UIUtil.showError(this, "加载试卷失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateTableDisplay() {
        System.out.println("DEBUG: TeacherPaperPanel.updateTableDisplay() called, row count: " + paperTableModel.getRowCount());
        // 如果表格没有数据，显示"暂无试卷"提示
        if (paperTableModel.getRowCount() == 0) {
            System.out.println("DEBUG: No papers found, showing '暂无试卷' message");
            showNoDataMessage();
        } else {
            System.out.println("DEBUG: Papers found, showing table with " + paperTableModel.getRowCount() + " rows");
            // 显示表头
            paperManagementTable.getTableHeader().setVisible(true);
            // 确保显示表格
            showTable();
        }
    }
    
    private void showTable() {
        System.out.println("DEBUG: TeacherPaperPanel.showTable() called");
        // 确保表格可见
        paperManagementTable.setVisible(true);
        
        // 直接使用tablePanel，这是在initComponents中创建的表格容器
        // 不再动态查找父容器，避免重复添加组件
        if (tablePanel != null) {
            // 确保tablePanel的布局正确
            tablePanel.removeAll();
            tablePanel.setLayout(new BorderLayout(0, 15));
            
            // 将表格添加到tablePanel中（这个操作应该只在初始化时做一次）
            // 但在这里我们只是确保表格显示正确
            JScrollPane scrollPane = new JScrollPane(paperManagementTable);
            tablePanel.add(scrollPane, BorderLayout.CENTER);
            
            // 强制重新验证和重绘
            tablePanel.revalidate();
            tablePanel.repaint();
        }
        
        // 同时对表格组件进行重绘以确保显示更新
        paperManagementTable.revalidate();
        paperManagementTable.repaint();
        System.out.println("DEBUG: Called revalidate/repaint on tablePanel and paperManagementTable");
    }
    
    private void showNoDataMessage() {
        System.out.println("DEBUG: TeacherPaperPanel.showNoDataMessage() called");
        // 隐藏表格组件
        paperManagementTable.setVisible(false);
        
        // 创建"暂无试卷"提示标签
        JLabel noDataLabel = new JLabel("暂无试卷", SwingConstants.CENTER);
        noDataLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        noDataLabel.setForeground(new Color(150, 150, 150));
        noDataLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // 直接使用tablePanel，这是在initComponents中创建的表格容器
        // 不再动态查找父容器，避免重复添加组件
        if (tablePanel != null) {
            tablePanel.removeAll();
            tablePanel.setLayout(new BorderLayout(0, 15));
            tablePanel.add(noDataLabel, BorderLayout.CENTER);
            
            tablePanel.revalidate();
            tablePanel.repaint();
        }
        
        System.out.println("DEBUG: Added noDataLabel to tablePanel and called revalidate/repaint");
    }
    
    /**
     * 获取表格模型（供主框架使用）
     */
    public DefaultTableModel getTableModel() {
        return paperTableModel;
    }
}