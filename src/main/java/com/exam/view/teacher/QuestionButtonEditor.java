package com.exam.view.teacher;

import com.exam.util.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 题目表格 - 操作列按钮编辑器
 */
public class QuestionButtonEditor extends DefaultCellEditor {
    private JPanel panel;
    private JButton editButton;
    private JButton deleteButton;
    private int currentRow;
    private JTable table;
    
    // 回调接口
    public interface QuestionButtonCallback {
        void onEdit(int row);
        void onDelete(int row);
    }
    
    private QuestionButtonCallback callback;

    public QuestionButtonEditor(JTable table, QuestionButtonCallback callback) {
        super(new JCheckBox());
        this.table = table;
        this.callback = callback;

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel.setBackground(Color.WHITE);

        editButton = new JButton("编辑");
        editButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        editButton.setBackground(UIUtil.PRIMARY_COLOR);
        editButton.setForeground(Color.BLACK);
        editButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        editButton.setFocusPainted(false);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> {
            fireEditingStopped();
            if (callback != null) {
                callback.onEdit(currentRow);
            }
        });

        deleteButton = new JButton("删除");
        deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        deleteButton.setBackground(UIUtil.DANGER_COLOR);
        deleteButton.setForeground(Color.BLACK);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.addActionListener(e -> {
            fireEditingStopped();
            if (callback != null) {
                callback.onDelete(currentRow);
            }
        });

        panel.add(editButton);
        panel.add(deleteButton);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        currentRow = row;
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
}
