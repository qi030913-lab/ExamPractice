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
    private JButton publishButton;

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

        add(viewButton);
        add(editButton);
        add(publishButton);
        add(deleteButton);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        // 根据试卷发布状态更新按钮文字和颜色
        // 从表格数据中获取发布状态，而不是每次都查询数据库
        try {
            // 获取表格模型
            DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
            // 获取试卷状态列（假设状态列在索引5的位置）
            String status = (String) tableModel.getValueAt(row, 5);
            // 检查状态是否包含"已发布"文本
            if (status != null && status.contains("已发布")) {
                publishButton.setText("取消发布");
                publishButton.setBackground(new Color(255, 152, 0)); // 橙色表示取消发布
            } else {
                publishButton.setText("发布");
                publishButton.setBackground(UIUtil.SUCCESS_COLOR); // 绿色表示发布
            }
        } catch (Exception ex) {
            // 如果获取失败，使用默认状态
            publishButton.setText("发布");
            publishButton.setBackground(UIUtil.SUCCESS_COLOR);
        }
        
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(Color.WHITE);
        }
        return this;
    }
}