-- 更新题目类型枚举，添加应用题、算法设计题、简答题和综合题支持
-- 执行日期：2025-12-29

USE exam_system;

-- 1. 修改 question 表的 question_type 字段，添加新的题目类型
ALTER TABLE question 
MODIFY COLUMN question_type ENUM('SINGLE', 'MULTIPLE', 'JUDGE', 'BLANK', 'APPLICATION', 'ALGORITHM', 'SHORT_ANSWER', 'COMPREHENSIVE') 
NOT NULL COMMENT '题目类型：单选、多选、判断、填空、应用题、算法设计题、简答题、综合题';

-- 2. 修改 question 表的 correct_answer 字段，支持较长答案
ALTER TABLE question 
MODIFY COLUMN correct_answer TEXT NOT NULL COMMENT '正确答案';

-- 3. 修改 answer_record 表的 student_answer 字段，支持较长答案
ALTER TABLE answer_record 
MODIFY COLUMN student_answer TEXT COMMENT '学生答案';

-- 验证修改
SHOW COLUMNS FROM question LIKE 'question_type';
SHOW COLUMNS FROM question LIKE 'correct_answer';
SHOW COLUMNS FROM answer_record LIKE 'student_answer';
