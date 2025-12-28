package com.exam.view.teacher.ui.components;

import com.exam.model.Paper;
import com.exam.service.PaperService;
import com.exam.util.UIUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * 试卷表格 - 操作列按钮编辑器
 */
public class PaperButtonEditor extends DefaultCellEditor {
    private JPanel panel;
    private JButton viewButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton publishButton;
    private int currentRow;
    private JTable table;
    private DefaultTableModel tableModel;
    private PaperService paperService;
    
    // 回调接口
    public interface PaperButtonCallback {
        void onView(int row);
        void onEdit(int row);
        void onDelete(int row);
        void onTogglePublish(int row);
    }
    
    private PaperButtonCallback callback;

    public PaperButtonEditor(JTable table, DefaultTableModel tableModel, 
                            PaperService paperService, PaperButtonCallback callback) {
        super(new JCheckBox());
        this.table = table;
        this.tableModel = tableModel;
        this.paperService = paperService;
        this.callback = callback;

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        panel.setBackground(Color.WHITE);

        viewButton = new JButton("查看");
        viewButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        viewButton.setBackground(new Color(52, 152, 219));
        viewButton.setForeground(Color.BLACK);
        viewButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1), // 外边框
                BorderFactory.createEmptyBorder(4, 9, 4, 9) // 内边距
        ));
        viewButton.setFocusPainted(false);
        viewButton.setContentAreaFilled(true);
        viewButton.setOpaque(true);
        viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewButton.addActionListener(e -> {
            fireEditingStopped();
            if (callback != null) {
                callback.onView(currentRow);
            }
        });

        editButton = new JButton("编辑");
        editButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        editButton.setBackground(UIUtil.PRIMARY_COLOR);
        editButton.setForeground(Color.BLACK);
        editButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1), // 外边框
                BorderFactory.createEmptyBorder(4, 9, 4, 9) // 内边距
        ));
        editButton.setFocusPainted(false);
        editButton.setContentAreaFilled(true);
        editButton.setOpaque(true);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> {
            fireEditingStopped();
            if (callback != null) {
                callback.onEdit(currentRow);
            }
        });

        deleteButton = new JButton("删除");
        deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        deleteButton.setBackground(UIUtil.DANGER_COLOR);
        deleteButton.setForeground(Color.BLACK);
        deleteButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1), // 外边框
                BorderFactory.createEmptyBorder(4, 9, 4, 9) // 内边距
        ));
        deleteButton.setFocusPainted(false);
        deleteButton.setContentAreaFilled(true);
        deleteButton.setOpaque(true);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.addActionListener(e -> {
            fireEditingStopped();
            if (callback != null) {
                callback.onDelete(currentRow);
            }
        });

        publishButton = new JButton("发布");
        publishButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        publishButton.setBackground(UIUtil.SUCCESS_COLOR);
        publishButton.setForeground(Color.BLACK);
        publishButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1), // 外边框
                BorderFactory.createEmptyBorder(4, 9, 4, 9) // 内边距
        ));
        publishButton.setFocusPainted(false);
        publishButton.setContentAreaFilled(true);
        publishButton.setOpaque(true);
        publishButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        publishButton.addActionListener(e -> {
            fireEditingStopped();
            if (callback != null) {
                callback.onTogglePublish(currentRow);
            }
        });

        panel.add(viewButton);
        panel.add(editButton);
        panel.add(publishButton);
        panel.add(deleteButton);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        currentRow = row;

        // 根据试卷发布状态更新按钮文字和颜色
        try {
            String paperName = (String) tableModel.getValueAt(row, 0);
            Paper paper = paperService.getPaperByName(paperName);
            if (paper != null && paper.getIsPublished() != null && paper.getIsPublished()) {
                publishButton.setText("取消发布");
                publishButton.setBackground(new Color(255, 152, 0));
            } else {
                publishButton.setText("发布");
                publishButton.setBackground(UIUtil.SUCCESS_COLOR);
            }
            // 确保按钮边框样式一致
            publishButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1), // 外边框
                    BorderFactory.createEmptyBorder(4, 9, 4, 9) // 内边距
            ));
        } catch (Exception ex) {
            // 如果获取失败，使用默认状态
            publishButton.setText("发布");
            publishButton.setBackground(UIUtil.SUCCESS_COLOR);
            // 确保按钮边框样式一致
            publishButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1), // 外边框
                    BorderFactory.createEmptyBorder(4, 9, 4, 9) // 内边距
            ));
        }

        if (isSelected) {
            panel.setBackground(table.getSelectionBackground());
        } else {
            panel.setBackground(Color.WHITE);
        }
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return "";
    }
    
    // 添加内部类：复选框渲染器
    public static class QuestionCheckBoxRenderer implements javax.swing.table.TableCellRenderer {
        private final JCheckBox checkBox = new JCheckBox();
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                                                      boolean hasFocus, int row, int column) {
            checkBox.setSelected(Boolean.TRUE.equals(value));
            if (isSelected) {
                checkBox.setBackground(table.getSelectionBackground());
                checkBox.setForeground(table.getSelectionForeground());
            } else {
                checkBox.setBackground(table.getBackground());
                checkBox.setForeground(table.getForeground());
            }
            return checkBox;
        }
    }
    
    // 添加内部类：复选框编辑器
    public static class QuestionCheckBoxEditor extends DefaultCellEditor {
        private final JCheckBox checkBox = new JCheckBox();
        
        public QuestionCheckBoxEditor() {
            super(new JCheckBox());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, 
                                                    int row, int column) {
            checkBox.setSelected(Boolean.TRUE.equals(value));
            return checkBox;
        }
        
        @Override
        public Object getCellEditorValue() {
            return checkBox.isSelected();
        }
    }
}