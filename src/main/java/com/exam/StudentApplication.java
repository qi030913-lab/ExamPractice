package com.exam;

import com.exam.view.student.StudentLoginFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;

/**
 * 学生端启动类
 * 独立启动学生登录界面
 * 
 * @author 在线考试系统开发组
 * @version 1.0
 */
public class StudentApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentApplication.class);
    
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
            LOGGER.warn("Failed to set Nimbus look and feel, fallback to system look and feel", e);
            // 如果Nimbus不可用，使用系统默认外观
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                LOGGER.error("Failed to set system look and feel", ex);
            }
        }
        
        // 启动学生登录界面
        SwingUtilities.invokeLater(() -> {
            StudentLoginFrame studentLoginFrame = new StudentLoginFrame();
            studentLoginFrame.setVisible(true);
        });
    }
}

