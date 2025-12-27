package com.exam.view.teacher;

/**
 * 教师端常量定义
 */
public class TeacherConstants {
    /**
     * 科目列表（包含"全部"选项）
     */
    public static final String[] SUBJECTS = {
        "全部", "Java", "Vue", "数据结构", 
        "马克思主义", "计算机网络", "操作系统", "数据库"
    };
    
    /**
     * 获取不包含"全部"的科目列表
     */
    public static String[] getSubjectsWithoutAll() {
        String[] result = new String[SUBJECTS.length - 1];
        System.arraycopy(SUBJECTS, 1, result, 0, SUBJECTS.length - 1);
        return result;
    }
}
