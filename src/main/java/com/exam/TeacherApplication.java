package com.exam;

import com.exam.view.teacher.TeacherLoginFrame;
import javax.swing.*;

/**
 * 教师端启动类
 * 独立启动教师登录界面
 * 
 * @author 在线考试系统开发组
 * @version 1.0
 */
public class TeacherApplication {
    
    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 启动教师登录界面
        SwingUtilities.invokeLater(() -> {
            TeacherLoginFrame teacherLoginFrame = new TeacherLoginFrame();
            teacherLoginFrame.setVisible(true);
        });
    }
}
