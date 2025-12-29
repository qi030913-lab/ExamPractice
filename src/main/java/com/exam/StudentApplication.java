package com.exam;

import com.exam.view.student.StudentLoginFrame;
import javax.swing.*;

/**
 * 学生端启动类
 * 独立启动学生登录界面
 * 
 * @author 在线考试系统开发组
 * @version 1.0
 */
public class StudentApplication {
    
    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 启动学生登录界面
        SwingUtilities.invokeLater(() -> {
            StudentLoginFrame studentLoginFrame = new StudentLoginFrame();
            studentLoginFrame.setVisible(true);
        });
    }
}
