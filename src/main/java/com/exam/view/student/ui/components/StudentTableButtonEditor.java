package com.exam.view.student.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * 学生端表格按钮编辑器 - 通用的表格按钮编辑器
 */
public class StudentTableButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private String label;
    private boolean isPushed;
    private int currentRow;
    private ButtonActionListener actionListener;

    public interface ButtonActionListener {
        void onButtonClick(int row);
    }

    public StudentTableButtonEditor(JCheckBox checkBox, ButtonActionListener actionListener) {
        super(checkBox);
        this.actionListener = actionListener;
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
        if (isPushed && actionListener != null) {
            actionListener.onButtonClick(currentRow);
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