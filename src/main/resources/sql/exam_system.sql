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
    question_type ENUM('SINGLE', 'MULTIPLE', 'JUDGE', 'BLANK') NOT NULL COMMENT '题目类型：单选、多选、判断、填空',
    subject VARCHAR(50) NOT NULL COMMENT '科目',
    content TEXT NOT NULL COMMENT '题目内容',
    option_a VARCHAR(500) COMMENT '选项A',
    option_b VARCHAR(500) COMMENT '选项B',
    option_c VARCHAR(500) COMMENT '选项C',
    option_d VARCHAR(500) COMMENT '选项D',
    correct_answer VARCHAR(10) NOT NULL COMMENT '正确答案',
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
    creator_id INT COMMENT '创建者ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_subject (subject),
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
    student_answer VARCHAR(10) COMMENT '学生答案',
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

-- 插入默认用户（密码统一为123456）
INSERT INTO user (real_name, student_number, password, role, email, gender, status) VALUES
('张老师', 'T001', '123456', 'TEACHER', 'teacher1@exam.com', 'FEMALE', 'ACTIVE'),
('李明', 'S2023001', '123456', 'STUDENT', 'student1@exam.com', 'MALE', 'ACTIVE'),
('王芳', 'S2023002', '123456', 'STUDENT', 'student2@exam.com', 'FEMALE', 'ACTIVE');

-- 插入示例题目
INSERT INTO question (question_type, subject, content, option_a, option_b, option_c, option_d, correct_answer, score, difficulty, analysis, creator_id) VALUES
('SINGLE', 'Java', 'Java中哪个关键字可以用来定义常量？', 'const', 'final', 'static', 'constant', 'B', 5, 'EASY', 'final关键字用于定义常量，被final修饰的变量不可改变。', 2),
('SINGLE', 'Java', '下列哪个不是Java的基本数据类型？', 'int', 'float', 'boolean', 'String', 'D', 5, 'EASY', 'String是引用类型，不是基本数据类型。', 2),
('SINGLE', 'Java', 'Java中实现多态的机制是什么？', '封装', '继承', '抽象', '接口', 'B', 5, 'MEDIUM', '多态是通过继承和方法重写实现的。', 2),
('MULTIPLE', 'Java', 'Java中哪些是访问修饰符？', 'public', 'private', 'protected', 'final', 'ABC', 10, 'EASY', 'public、private、protected是访问修饰符，final是最终修饰符。', 2),
('MULTIPLE', 'Java', '关于ArrayList和LinkedList说法正确的是？', 'ArrayList基于数组实现', 'LinkedList基于链表实现', 'ArrayList随机访问快', 'LinkedList插入删除快', 'ABCD', 10, 'MEDIUM', '两者各有优劣，ArrayList适合查询，LinkedList适合增删。', 2),
('JUDGE', 'Java', 'Java中int和Integer可以直接进行==比较。', '正确', '错误', NULL, NULL, 'B', 5, 'MEDIUM', 'int是基本类型，Integer是包装类，直接==比较会有自动拆箱问题，不推荐。', 2),
('JUDGE', 'Java', 'Java支持多继承。', '正确', '错误', NULL, NULL, 'B', 5, 'EASY', 'Java不支持类的多继承，但支持接口的多实现。', 2),
('SINGLE', 'Java', '下列哪个集合是线程安全的？', 'ArrayList', 'HashMap', 'Vector', 'LinkedList', 'C', 5, 'MEDIUM', 'Vector是线程安全的，但性能较差，推荐使用Collections.synchronizedList。', 2);

-- 插入示例试卷
INSERT INTO paper (paper_name, subject, total_score, duration, pass_score, description, creator_id) VALUES
('Java基础测试', 'Java', 100, 60, 60, 'Java基础知识测试，涵盖基本语法和面向对象', 2);

-- 插入试卷题目关联
INSERT INTO paper_question (paper_id, question_id, question_order) VALUES
(1, 1, 1),
(1, 2, 2),
(1, 3, 3),
(1, 4, 4),
(1, 5, 5),
(1, 6, 6),
(1, 7, 7),
(1, 8, 8);
