package com.exam;

import com.exam.view.student.StudentLoginFrame;
import com.exam.view.teacher.TeacherLoginFrame;

import javax.swing.*;

/**
 * 多线程启动类
 * 使用多线程同时启动学生端和教师端
 */
public class MultiThreadApplication {
    
    public static void main(String[] args) {
        System.out.println("在线考试系统多线程启动");
        System.out.println("主线程：" + Thread.currentThread().getName());
        
        // 设置现代化外观 - Nimbus主题
        setLookAndFeel();
        
        // 创建学生端启动线程
        Thread studentThread = new Thread(() -> {
            System.out.println("学生端线程启动：" + Thread.currentThread().getName());
            SwingUtilities.invokeLater(() -> {
                StudentLoginFrame studentFrame = new StudentLoginFrame();
                // 设置学生端窗口位置（左侧）
                studentFrame.setLocation(100, 150);
                studentFrame.setVisible(true);
                System.out.println("学生端界面已创建");
            });
        }, "StudentThread");
        
        // 创建教师端启动线程
        Thread teacherThread = new Thread(() -> {
            System.out.println("教师端线程启动：" + Thread.currentThread().getName());
            SwingUtilities.invokeLater(() -> {
                TeacherLoginFrame teacherFrame = new TeacherLoginFrame();
                // 设置教师端窗口位置（右侧）
                teacherFrame.setLocation(850, 150);
                teacherFrame.setVisible(true);
                System.out.println("教师端界面已创建");
            });
        }, "TeacherThread");
        
        // 设置线程优先级（可选）
        studentThread.setPriority(Thread.NORM_PRIORITY);
        teacherThread.setPriority(Thread.NORM_PRIORITY);
        
        // 启动两个线程
        try {
            studentThread.start();
            // 稍微延迟，避免窗口重叠
            Thread.sleep(100);
            teacherThread.start();
            
            System.out.println("两个线程已启动，等待窗口创建完成...");
            
            // 等待两个线程完成（可选）
            studentThread.join();
            teacherThread.join();
            
            System.out.println("所有界面启动完成！");
            
        } catch (InterruptedException e) {
            System.err.println("线程启动过程中断：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 设置系统外观
     */
    private static void setLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    System.out.println("使用Nimbus主题");
                    return;
                }
            }
            // 如果Nimbus不可用，使用系统默认外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("使用系统默认主题");
        } catch (Exception e) {
            System.err.println("设置外观失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
