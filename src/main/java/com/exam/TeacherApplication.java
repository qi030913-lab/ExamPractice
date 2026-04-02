package com.exam;

import com.exam.view.teacher.TeacherLoginFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;

/**
 * 教师端 Swing 兼容启动类
 * 当前正式桌面入口已迁移到 Electron + Vue3，此类仅保留给旧版兼容场景使用。
 * 
 * @author 在线考试系统开发组
 * @version 1.0
 */
public class TeacherApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherApplication.class);
    
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
        
        // 启动教师登录界面
        SwingUtilities.invokeLater(() -> {
            TeacherLoginFrame teacherLoginFrame = new TeacherLoginFrame();
            teacherLoginFrame.setVisible(true);
        });
    }
}

