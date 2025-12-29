package com.exam.view.teacher.ui.components;

import com.exam.util.UIUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * 试卷表格 - 操作列按钮渲染器
 */
public class PaperButtonRenderer extends JPanel implements TableCellRenderer {
    private JButton viewButton;
    private JButton editButton;
    private JButton deleteButton;

    public PaperButtonRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
        setBackground(Color.WHITE);

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

        add(viewButton);
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