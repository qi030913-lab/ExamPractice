package com.exam.view.teacher.ui.components;

import com.exam.model.ExamRecord;
import com.exam.model.User;
import com.exam.service.ExamService;
import com.exam.service.UserService;
import com.exam.util.UIUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 教师端 - 学生管理面板
 */
public class TeacherStudentPanel extends JPanel {
    private final UserService userService;
    private final ExamService examService;
    private final com.exam.view.teacher.TeacherMainFrame mainFrame;
    
    private JTable studentTable;
    private JTable examRecordTable;
    private DefaultTableModel studentTableModel;
    private DefaultTableModel examRecordTableModel;

    public TeacherStudentPanel(com.exam.view.teacher.TeacherMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userService = new UserService();
        this.examService = new ExamService();
        initComponents();
        loadStudentData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5)); // 减小间距
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 减小边距

        // 标题
        JLabel titleLabel = new JLabel("学生管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(UIUtil.TEXT_COLOR);
        add(titleLabel, BorderLayout.NORTH);

        // 主内容区域
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setResizeWeight(0.5); // 设置左右两侧平分剩余空间
        
        // 左侧 - 学生列表
        splitPane.setLeftComponent(createStudentListPanel());

        // 右侧 - 考试记录
        splitPane.setRightComponent(createExamRecordPanel());

        add(splitPane, BorderLayout.CENTER);
        
        // 在界面显示后设置分隔条位置
        SwingUtilities.invokeLater(() -> {
            splitPane.setDividerLocation(0.5);
        });
    }

    private JPanel createStudentListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);

        // 学生列表标题
        JLabel studentListLabel = new JLabel("学生列表");
        studentListLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        studentListLabel.setForeground(UIUtil.TEXT_COLOR);
        panel.add(studentListLabel, BorderLayout.NORTH);

        // 学生表格 - 修改列标题，移除ID和角色列
        String[] columnNames = {"ID", "姓名", "学号", "注册时间"}; // 添加ID列但隐藏它
        studentTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(studentTableModel);
        studentTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        studentTable.getTableHeader().setBackground(UIUtil.PRIMARY_COLOR);
        studentTable.getTableHeader().setForeground(Color.BLACK);
        studentTable.setGridColor(UIUtil.PRIMARY_COLOR); // 设置表格线为蓝色
        studentTable.setSelectionBackground(new Color(230, 240, 255)); // 设置选中行背景色
        studentTable.setSelectionForeground(Color.BLACK); // 设置选中行文字颜色
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setRowHeight(25);
        studentTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        // 隐藏ID列
        studentTable.getColumnModel().getColumn(0).setMinWidth(0);
        studentTable.getColumnModel().getColumn(0).setMaxWidth(0);
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // 添加选择监听器
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = studentTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // 从隐藏的ID列获取用户ID
                    int userId = (Integer) studentTable.getValueAt(selectedRow, 0);
                    loadExamRecordData(userId);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtil.PRIMARY_COLOR, 1)); // 设置边框为蓝色
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadStudentData() {
        try {
            // 清空现有数据
            studentTableModel.setRowCount(0);

            // 获取所有学生用户
            List<User> students = userService.getStudents();

            // 添加数据到表格
            for (User student : students) {
                Object[] rowData = {
                    student.getUserId(),          // ID - 第0列
                    student.getRealName(),        // 姓名 - 第1列
                    student.getStudentNumber(),   // 学号 - 第2列
                    student.getCreateTime()       // 注册时间 - 第3列
                };
                studentTableModel.addRow(rowData);
            }
        } catch (Exception e) {
            UIUtil.showError(this, "加载学生数据失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private JPanel createExamRecordPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);

        // 考试记录标题
        JLabel examRecordLabel = new JLabel("考试记录");
        examRecordLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        examRecordLabel.setForeground(UIUtil.TEXT_COLOR);
        panel.add(examRecordLabel, BorderLayout.NORTH);

        // 考试记录表格 - 删除状态列
        String[] columnNames = {"试卷名称", "开始时间", "结束时间", "用时(分钟)", "得分"};
        examRecordTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        examRecordTable = new JTable(examRecordTableModel);
        examRecordTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        examRecordTable.getTableHeader().setBackground(UIUtil.PRIMARY_COLOR);
        examRecordTable.getTableHeader().setForeground(Color.BLACK);
        examRecordTable.setGridColor(UIUtil.PRIMARY_COLOR); // 设置表格线为蓝色
        examRecordTable.setSelectionBackground(new Color(230, 240, 255)); // 设置选中行背景色
        examRecordTable.setSelectionForeground(Color.BLACK); // 设置选中行文字颜色
        examRecordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        examRecordTable.setRowHeight(25);
        examRecordTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(examRecordTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtil.PRIMARY_COLOR, 1)); // 设置边框为蓝色
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadExamRecordData(int userId) {
        try {
            System.out.println("正在加载用户ID为 " + userId + " 的考试记录...");
            
            // 获取该学生的考试记录
            List<ExamRecord> examRecords = examService.getStudentExamRecords(userId);
            System.out.println("找到 " + examRecords.size() + " 条考试记录");

            // 通过组件层次结构直接获取考试记录面板
            // 获取主面板中的JSplitPane
            JSplitPane splitPane = null;
            for (Component comp : this.getComponents()) {
                if (comp instanceof JSplitPane) {
                    splitPane = (JSplitPane) comp;
                    break;
                }
            }
            
            if (splitPane != null) {
                // 获取右侧面板（考试记录面板）
                JPanel rightPanel = (JPanel) splitPane.getRightComponent();
                if (rightPanel != null) {
                    System.out.println("右侧面板组件数量: " + rightPanel.getComponentCount());
                    if (examRecords.isEmpty()) {
                        System.out.println("没有考试记录，显示提示信息");
                        // 如果没有考试记录，显示提示信息而不是表格
                        // 创建提示标签
                        JLabel noRecordLabel = new JLabel("暂无考试记录", SwingConstants.CENTER);
                        noRecordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
                        noRecordLabel.setForeground(new Color(150, 150, 150));
                        noRecordLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
                        
                        // 移除除标题外的所有组件，保留标题
                        Component[] components = rightPanel.getComponents();
                        for (int i = components.length - 1; i >= 0; i--) {
                            if (!(components[i] instanceof JLabel && ((JLabel) components[i]).getText().equals("考试记录"))) {
                                rightPanel.remove(components[i]);
                            }
                        }
                        
                        // 添加提示标签到中心位置
                        rightPanel.add(noRecordLabel, BorderLayout.CENTER);
                        rightPanel.revalidate();
                        rightPanel.repaint();
                    } else {
                        System.out.println("有考试记录，填充表格");
                        // 清空现有数据
                        examRecordTableModel.setRowCount(0);

                        // 添加数据到表格
                        for (ExamRecord record : examRecords) {
                            System.out.println("处理考试记录: " + record.getRecordId() + ", 状态: " + record.getStatus());
                            // 确保试卷信息已加载
                            if (record.getPaper() == null) {
                                System.out.println("试卷信息为空，跳过记录ID: " + record.getRecordId());
                                continue; // 跳过试卷信息为空的记录
                            }
                            System.out.println("试卷信息: " + record.getPaper().getPaperName());
                            long durationMinutes = java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMinutes();
                            Object[] rowData = {
                                record.getPaper().getPaperName(),
                                record.getStartTime(),
                                record.getEndTime(),
                                durationMinutes,
                                record.getScore()
                            };
                            examRecordTableModel.addRow(rowData);
                        }
                        
                        System.out.println("表格中记录数量: " + examRecordTableModel.getRowCount());
                        
                        // 移除除标题外的所有组件，保留标题
                        Component[] components = rightPanel.getComponents();
                        for (int i = components.length - 1; i >= 0; i--) {
                            if (!(components[i] instanceof JLabel && ((JLabel) components[i]).getText().equals("考试记录"))) {
                                rightPanel.remove(components[i]);
                            }
                        }
                        
                        // 添加表格滚动面板
                        JScrollPane scrollPane = new JScrollPane(examRecordTable);
                        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtil.PRIMARY_COLOR, 1));
                        rightPanel.add(scrollPane, BorderLayout.CENTER);
                        rightPanel.revalidate();
                        rightPanel.repaint();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("加载考试记录时出错: " + e.getMessage());
            e.printStackTrace();
            UIUtil.showError(this, "加载考试记录失败：" + e.getMessage());
        }
    }

    public void refreshData() {
        loadStudentData();
        examRecordTableModel.setRowCount(0); // 清空考试记录表
    }
}