package com.exam;

import com.exam.view.LoginFrame;
import javax.swing.*;

/**
 * 在线考试系统主程序
 * 
 * 系统功能：
 * 1. 用户登录（学生/教师）
 * 2. 教师端：题库管理、试卷管理
 * 3. 学生端：在线考试、成绩查询
 * 4. 自动判分、倒计时、自动交卷
 * 
 * 涉及知识点：
 * - 面向对象编程（封装、继承、多态）
 * - 集合框架（List、Map、Set）
 * - 异常处理机制
 * - 数据库编程（JDBC）
 * - 图形化界面（Swing）
 * - 多线程（倒计时）
 * 
 * @author 在线考试系统开发组
 * @version 1.0
 */
public class ExamSystemApplication {
    
    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 启动登录界面
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
