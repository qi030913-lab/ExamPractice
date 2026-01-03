-- ========================================
-- 数据库更新脚本：添加英语新题型支持
-- 执行时间：2026-01-03
-- 功能：在question表的question_type字段中添加英语相关题型
-- ========================================

USE exam_system;

-- 修改question表的question_type字段，添加新的英语题型
ALTER TABLE question 
MODIFY COLUMN question_type ENUM(
    'SINGLE', 
    'MULTIPLE', 
    'JUDGE', 
    'BLANK', 
    'APPLICATION', 
    'ALGORITHM', 
    'SHORT_ANSWER', 
    'COMPREHENSIVE', 
    'ESSAY', 
    'MATERIAL_ANALYSIS',
    'CLOZE',
    'READING_ANALYSIS',
    'ENGLISH_TO_CHINESE',
    'CHINESE_TO_ENGLISH',
    'WRITING'
) NOT NULL COMMENT '题目类型：单选、多选、判断、填空、应用题、算法设计题、简答题、综合题、论述题、材料分析题、选词填空、阅读分析、英译汉、汉译英、写作';

-- 验证修改是否成功
SHOW COLUMNS FROM question LIKE 'question_type';

SELECT '数据库更新完成！已添加英语新题型支持：CLOZE(选词填空)、READING_ANALYSIS(阅读分析)、ENGLISH_TO_CHINESE(英译汉)、CHINESE_TO_ENGLISH(汉译英)、WRITING(写作)' AS message;
