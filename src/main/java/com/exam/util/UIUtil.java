package com.exam.util;

import javax.swing.*;
import java.awt.*;

/**
 * UI工具类
 * 提供统一的UI样式和组件创建方法
 */
public class UIUtil {
    
    // 主题颜色 - 根据图片设计
    public static final Color PRIMARY_COLOR = new Color(52, 152, 219); // 蓝色主题
    public static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    public static final Color DANGER_COLOR = new Color(231, 76, 60);
    public static final Color WARNING_COLOR = new Color(255, 193, 7); // 金黄色
    public static final Color BACKGROUND_COLOR = new Color(240, 244, 248); // 浅灰蓝背景
    public static final Color SIDEBAR_COLOR = new Color(232, 240, 254); // 侧边栏背景
    public static final Color SIDEBAR_ACTIVE = new Color(66, 153, 225); // 侧边栏选中
    public static final Color TEXT_COLOR = new Color(44, 62, 80);
    public static final Color TEXT_GRAY = new Color(108, 117, 125);
    
    // 字体
    public static final Font TITLE_FONT = new Font("微软雅黑", Font.BOLD, 24);
    public static final Font HEADING_FONT = new Font("微软雅黑", Font.BOLD, 16);
    public static final Font NORMAL_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("微软雅黑", Font.PLAIN, 12);
    
    /**
     * 创建主按钮
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(NORMAL_FONT);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    /**
     * 创建成功按钮
     */
    public static JButton createSuccessButton(String text) {
        JButton button = new JButton(text);
        button.setFont(NORMAL_FONT);
        button.setBackground(SUCCESS_COLOR);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    /**
     * 创建危险按钮
     */
    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(NORMAL_FONT);
        button.setBackground(DANGER_COLOR);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    /**
     * 显示信息对话框
     */
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 显示错误对话框
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "错误", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * 显示警告对话框
     */
    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "警告", JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * 显示确认对话框
     */
    public static boolean showConfirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "确认", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * 居中显示窗口
     */
    public static void centerWindow(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - window.getWidth()) / 2;
        int y = (screenSize.height - window.getHeight()) / 2;
        window.setLocation(x, y);
    }
}
