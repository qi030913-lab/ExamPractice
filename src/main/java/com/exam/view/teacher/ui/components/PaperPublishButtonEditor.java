package com.exam.view.teacher.ui.components;

import com.exam.model.Paper;
import com.exam.service.PaperService;
import com.exam.util.UIUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * 试卷表格 - 发布列按钮编辑器
 */
public class PaperPublishButtonEditor extends DefaultCellEditor {
    private JPanel panel;
    private JButton publishButton;
    private int currentRow;
    private JTable table;
    private DefaultTableModel tableModel;
    private PaperService paperService;
    
    // 回调接口
    public interface PublishButtonCallback {
        void onTogglePublish(int row);
    }
    
    private PublishButtonCallback callback;

    public PaperPublishButtonEditor(JTable table, DefaultTableModel tableModel, 
                                   PaperService paperService, PublishButtonCallback callback) {
        super(new JCheckBox());
        this.table = table;
        this.tableModel = tableModel;
        this.paperService = paperService;
        this.callback = callback;

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        panel.setBackground(Color.WHITE);

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

        panel.add(publishButton);
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
}
