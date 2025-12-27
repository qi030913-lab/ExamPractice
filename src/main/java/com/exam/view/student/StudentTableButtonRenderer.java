package com.exam.view.student;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * 学生端表格按钮渲染器 - 通用的表格按钮渲染器
 */
public class StudentTableButtonRenderer extends JButton implements TableCellRenderer {
    
    public StudentTableButtonRenderer() {
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