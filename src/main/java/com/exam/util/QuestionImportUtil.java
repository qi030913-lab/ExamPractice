package com.exam.util;

import com.exam.model.Question;
import com.exam.model.enums.QuestionType;
import com.exam.model.enums.Difficulty;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目导入工具类
 * 支持从文本文件导入题目
 */
public class QuestionImportUtil {

    /**
     * 从文本文件导入题目
     * 文件格式：
     * 题目类型|科目|题目内容|选项A|选项B|选项C|选项D|正确答案|分值|难度|解析
     * 
     * 示例：
     * SINGLE|Java|Java中哪个关键字用于定义常量？|const|final|static|constant|B|5|EASY|final关键字用于定义常量
     * MULTIPLE|Java|访问修饰符包括？|public|private|protected|final|ABC|10|MEDIUM|public、private、protected是访问修饰符
     * JUDGE|Java|Java支持多继承|正确|错误|||B|5|EASY|Java不支持类的多继承
     * BLANK|Java|Java中声明整数变量的关键字是____。||||int|5|EASY|使用int关键字声明整数类型
     * APPLICATION|Java|请编写一个Java程序，实现学生成绩管理系统|||||1.定义Student类 2.使用ArrayList存储|20|MEDIUM|考查面向对象和集合框架
     * ALGORITHM|算法|请设计一个算法，实现快速排序|||||1.选择基准元素 2.分区操作|25|HARD|快速排序是分治算法的应用
     * SHORT_ANSWER|Java|请简述Java中面向对象的三大特征|||||1.封装 2.继承 3.多态|15|MEDIUM|面向对象的核心概念
     * COMPREHENSIVE|Java|请设计并实现一个学生管理系统|||||1.架构设计 2.数据库设计|30|HARD|综合考查系统能力
     */
    public static List<Question> importFromTextFile(File file, Integer creatorId) throws Exception {
        List<Question> questions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                // 跳过空行和注释行
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }
                
                try {
                    Question question = parseLine(line, creatorId);
                    questions.add(question);
                } catch (Exception e) {
                    throw new Exception("第" + lineNumber + "行解析错误：" + e.getMessage());
                }
            }
        }
        
        if (questions.isEmpty()) {
            throw new Exception("文件中没有有效的题目数据");
        }
        
        return questions;
    }
    
    /**
     * 解析单行题目数据
     */
    private static Question parseLine(String line, Integer creatorId) throws Exception {
        String[] parts = line.split("\\|");
        
        if (parts.length < 8) {
            throw new Exception("数据格式不正确，至少需要8个字段（用|分隔）");
        }
        
        Question question = new Question();
        
        // 题目类型
        try {
            question.setQuestionType(QuestionType.valueOf(parts[0].trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new Exception("题目类型无效：" + parts[0] + "，应为SINGLE、MULTIPLE、JUDGE、BLANK、APPLICATION、ALGORITHM、SHORT_ANSWER或COMPREHENSIVE");
        }
        
        // 科目
        question.setSubject(parts[1].trim());
        if (question.getSubject().isEmpty()) {
            throw new Exception("科目不能为空");
        }
        
        // 题目内容
        question.setContent(parts[2].trim());
        if (question.getContent().isEmpty()) {
            throw new Exception("题目内容不能为空");
        }
        
        // 选项A-D
        question.setOptionA(parts.length > 3 ? parts[3].trim() : null);
        question.setOptionB(parts.length > 4 ? parts[4].trim() : null);
        question.setOptionC(parts.length > 5 ? parts[5].trim() : null);
        question.setOptionD(parts.length > 6 ? parts[6].trim() : null);
        
        // 正确答案
        question.setCorrectAnswer(parts[7].trim().toUpperCase());
        if (question.getCorrectAnswer().isEmpty()) {
            throw new Exception("正确答案不能为空");
        }
        
        // 分值
        try {
            question.setScore(parts.length > 8 && !parts[8].trim().isEmpty() 
                ? Integer.parseInt(parts[8].trim()) : 5);
        } catch (NumberFormatException e) {
            throw new Exception("分值必须是数字");
        }
        
        // 难度
        if (parts.length > 9 && !parts[9].trim().isEmpty()) {
            try {
                question.setDifficulty(Difficulty.valueOf(parts[9].trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                question.setDifficulty(Difficulty.MEDIUM);
            }
        } else {
            question.setDifficulty(Difficulty.MEDIUM);
        }
        
        // 解析
        question.setAnalysis(parts.length > 10 ? parts[10].trim() : "");
        
        // 创建者ID
        question.setCreatorId(creatorId);
        
        return question;
    }
    
    /**
     * 生成导入模板文件
     */
    public static void generateTemplate(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            
            writer.write("# 题目导入模板文件\n");
            writer.write("# 格式说明：题目类型|科目|题目内容|选项A|选项B|选项C|选项D|正确答案|分值|难度|解析\n");
            writer.write("# 题目类型：SINGLE(单选)、MULTIPLE(多选)、JUDGE(判断)、BLANK(填空)、APPLICATION(应用题)、ALGORITHM(算法设计题)、SHORT_ANSWER(简答题)、COMPREHENSIVE(综合题)\n");
            writer.write("# 难度：EASY(简单)、MEDIUM(中等)、HARD(困难)\n");
            writer.write("# 以#开头的行为注释，会被忽略\n");
            writer.write("\n");
            writer.write("# 示例题目：\n");
            writer.write("# 单选题示例\n");
            writer.write("SINGLE|Java|Java中哪个关键字可以用来定义常量？|const|final|static|constant|B|5|EASY|final关键字用于定义常量，被final修饰的变量不可改变。\n");
            writer.write("SINGLE|Java|下列哪个不是Java的基本数据类型？|int|float|boolean|String|D|5|EASY|String是引用类型，不是基本数据类型。\n");
            writer.write("\n");
            writer.write("# 多选题示例\n");
            writer.write("MULTIPLE|Java|Java中哪些是访问修饰符？|public|private|protected|final|ABC|10|EASY|public、private、protected是访问修饰符，final是最终修饰符。\n");
            writer.write("\n");
            writer.write("# 判断题示例\n");
            writer.write("JUDGE|Java|Java支持多继承|正确|错误|||B|5|EASY|Java不支持类的多继承，但支持接口的多实现。\n");
            writer.write("\n");
            writer.write("# 填空题示例\n");
            writer.write("BLANK|Java|Java中声明整数变量的关键字是____。||||int|5|EASY|使用int关键字声明整数类型。\n");
            writer.write("\n");
            writer.write("# 应用题示例（正确答案可以为关键要点描述）\n");
            writer.write("APPLICATION|Java|请编写一个Java程序，实现学生成绩管理系统，要求包含添加、删除、查询、修改功能。|||||1.定义Student类 2.使用ArrayList存储 3.实现CRUD操作 4.提供菜单交互|20|MEDIUM|考查面向对象、集合框架和系统设计能力。\n");
            writer.write("\n");
            writer.write("# 算法设计题示例（正确答案可以为算法描述或关键代码）\n");
            writer.write("ALGORITHM|算法|请设计一个算法，实现快速排序（Quick Sort），并分析其时间复杂度。|||||1.选择基准元素 2.分区操作 3.递归排序 4.时间复杂度O(nlogn)|25|HARD|快速排序是分治算法的典型应用，平均时间复杂度为O(nlogn)。\n");
            writer.write("\n");
            writer.write("# 简答题示例（正确答案为关键要点）\n");
            writer.write("SHORT_ANSWER|Java|请简述Java中面向对象的三大特征及其含义。|||||1.封装：将数据和方法封装在类中 2.继承：子类继承父类的属性和方法 3.多态：同一接口的不同实现|15|MEDIUM|面向对象的三大特征是Java的核心概念。\n");
            writer.write("\n");
            writer.write("# 综合题示例（正确答案为详细解答要点）\n");
            writer.write("COMPREHENSIVE|Java|请设计并实现一个学生管理系统，包括学生信息管理、成绩管理、课程管理等功能。|||||1.系统架构设计 2.数据库设计 3.类图设计 4.核心功能实现 5.异常处理|30|HARD|综合考查系统分析、设计和实现能力。\n");
        }
    }
    
    /**
     * 从文本文件导入题目
     * @param filePath 文件路径
     * @return 题目列表
     * @throws Exception 导入过程中的异常
     */
    public static List<Question> importQuestionsFromFile(String filePath) throws Exception {
        return importFromTextFile(new File(filePath), null);
    }
}
