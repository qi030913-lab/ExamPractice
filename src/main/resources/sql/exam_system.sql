-- ========================================
-- 在线考试系统数据库脚本
-- 根据代码实体类生成
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS exam_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE exam_system;

-- ========================================
-- 删除表（注意顺序：先删除子表，再删除父表）
-- ========================================
DROP TABLE IF EXISTS answer_record;
DROP TABLE IF EXISTS exam_record;
DROP TABLE IF EXISTS paper_question;
DROP TABLE IF EXISTS paper;
DROP TABLE IF EXISTS question;
DROP TABLE IF EXISTS user;

-- ========================================
-- 1. 用户表 (User)
-- ========================================
CREATE TABLE user (
    user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    student_number VARCHAR(50) NOT NULL UNIQUE COMMENT '学号（唯一标识）',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    role ENUM('STUDENT', 'TEACHER') NOT NULL COMMENT '角色',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    gender VARCHAR(20) COMMENT '性别',
    avatar_url VARCHAR(200) COMMENT '头像URL',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '账户状态',
    last_login_time TIMESTAMP NULL COMMENT '最后登录时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_student_number (student_number),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========================================
-- 2. 题库表 (Question)
-- ========================================
DROP TABLE IF EXISTS question;
CREATE TABLE question (
    question_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    question_type ENUM('SINGLE', 'MULTIPLE', 'JUDGE', 'BLANK', 'APPLICATION', 'ALGORITHM', 'SHORT_ANSWER', 'COMPREHENSIVE', 'ESSAY', 'MATERIAL_ANALYSIS') NOT NULL COMMENT '题目类型：单选、多选、判断、填空、应用题、算法设计题、简答题、综合题、论述题、材料分析题',
    subject VARCHAR(50) NOT NULL COMMENT '科目',
    content TEXT NOT NULL COMMENT '题目内容',
    option_a VARCHAR(500) COMMENT '选项A',
    option_b VARCHAR(500) COMMENT '选项B',
    option_c VARCHAR(500) COMMENT '选项C',
    option_d VARCHAR(500) COMMENT '选项D',
    correct_answer TEXT NOT NULL COMMENT '正确答案',
    score INT DEFAULT 5 COMMENT '分值',
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM' COMMENT '难度',
    analysis TEXT COMMENT '题目解析',
    creator_id INT COMMENT '创建者ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_question_type (question_type),
    INDEX idx_subject (subject),
    INDEX idx_difficulty (difficulty),
    FOREIGN KEY (creator_id) REFERENCES user(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';

-- ========================================
-- 3. 试卷表 (Paper)
-- ========================================
DROP TABLE IF EXISTS paper;
CREATE TABLE paper (
    paper_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '试卷ID',
    paper_name VARCHAR(100) NOT NULL COMMENT '试卷名称',
    subject VARCHAR(50) NOT NULL COMMENT '科目',
    total_score INT DEFAULT 100 COMMENT '总分',
    duration INT DEFAULT 90 COMMENT '考试时长(分钟)',
    pass_score INT DEFAULT 60 COMMENT '及格分数',
    description TEXT COMMENT '试卷描述',
    is_published BOOLEAN DEFAULT FALSE COMMENT '是否发布',
    creator_id INT COMMENT '创建者ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_subject (subject),
    INDEX idx_is_published (is_published),
    FOREIGN KEY (creator_id) REFERENCES user(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- ========================================
-- 4. 试卷题目关联表
-- ========================================
DROP TABLE IF EXISTS paper_question;
CREATE TABLE paper_question (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    paper_id INT NOT NULL COMMENT '试卷ID',
    question_id INT NOT NULL COMMENT '题目ID',
    question_order INT NOT NULL COMMENT '题目顺序',
    FOREIGN KEY (paper_id) REFERENCES paper(paper_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(question_id) ON DELETE CASCADE,
    UNIQUE KEY uk_paper_question (paper_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表';

-- ========================================
-- 5. 考试记录表 (ExamRecord)
-- ========================================
DROP TABLE IF EXISTS exam_record;
CREATE TABLE exam_record (
    record_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    student_id INT NOT NULL COMMENT '学生ID',
    paper_id INT NOT NULL COMMENT '试卷ID',
    start_time TIMESTAMP NULL COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    submit_time TIMESTAMP NULL COMMENT '提交时间',
    score DECIMAL(5,2) COMMENT '得分',
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'SUBMITTED', 'TIMEOUT') DEFAULT 'NOT_STARTED' COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_student_id (student_id),
    INDEX idx_paper_id (paper_id),
    INDEX idx_status (status),
    FOREIGN KEY (student_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (paper_id) REFERENCES paper(paper_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- ========================================
-- 6. 答题记录表 (AnswerRecord)
-- ========================================
DROP TABLE IF EXISTS answer_record;
CREATE TABLE answer_record (
    answer_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '答案ID',
    record_id INT NOT NULL COMMENT '考试记录ID',
    question_id INT NOT NULL COMMENT '题目ID',
    student_answer TEXT COMMENT '学生答案',
    is_correct BOOLEAN COMMENT '是否正确',
    score DECIMAL(5,2) DEFAULT 0 COMMENT '得分',
    INDEX idx_record_id (record_id),
    INDEX idx_question_id (question_id),
    FOREIGN KEY (record_id) REFERENCES exam_record(record_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(question_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题记录表';

-- ========================================
-- 初始化数据
-- ========================================




-- ========================================
-- 清除所有表的数据
-- ========================================
SET FOREIGN_KEY_CHECKS = 0;  -- 禁用外键检查
TRUNCATE TABLE answer_record;
TRUNCATE TABLE exam_record;
TRUNCATE TABLE paper_question;
TRUNCATE TABLE paper;
TRUNCATE TABLE question;
TRUNCATE TABLE user;
SET FOREIGN_KEY_CHECKS = 1;  -- 启用外键检查
