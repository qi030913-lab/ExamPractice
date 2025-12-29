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
        // 设置现代化外观 - Nimbus主题
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // 如果Nimbus不可用，使用系统默认外观
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        // 启动教师登录界面
        SwingUtilities.invokeLater(() -> {
            TeacherLoginFrame teacherLoginFrame = new TeacherLoginFrame();
            teacherLoginFrame.setVisible(true);
        });
    }
}
