package com.exam.view.teacher.ui.components;

import com.exam.util.UIUtil;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * 题目表格 - 操作列按钮渲染器
 */
public class QuestionButtonRenderer extends JPanel implements TableCellRenderer {
    private JButton editButton;
    private JButton deleteButton;

    public QuestionButtonRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        setBackground(Color.WHITE);

        editButton = new JButton("编辑");
        editButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        editButton.setBackground(UIUtil.PRIMARY_COLOR);
        editButton.setForeground(Color.BLACK);
        editButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        editButton.setFocusPainted(false);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        deleteButton = new JButton("删除");
        deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        deleteButton.setBackground(UIUtil.DANGER_COLOR);
        deleteButton.setForeground(Color.BLACK);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        add(editButton);
        add(deleteButton);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(Color.WHITE);
        }
        return this;
    }
}