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
-- Java题目
INSERT INTO question (question_type, subject, content, option_a, option_b, option_c, option_d, correct_answer, score, difficulty, analysis, creator_id) VALUES
('SINGLE', 'Java', 'Java中哪个关键字可以用来定义常量？', 'const', 'final', 'static', 'constant', 'B', 5, 'EASY', 'final关键字用于定义常量，被final修饰的变量不可改变。', 1),
('SINGLE', 'Java', '下列哪个不是Java的基本数据类型？', 'int', 'float', 'boolean', 'String', 'D', 5, 'EASY', 'String是引用类型，不是基本数据类型。', 1),
('SINGLE', 'Java', 'Java中实现多态的机制是什么？', '封装', '继承', '抽象', '接口', 'B', 5, 'MEDIUM', '多态是通过继承和方法重写实现的。', 1),
('MULTIPLE', 'Java', 'Java中哪些是访问修饰符？', 'public', 'private', 'protected', 'final', 'ABC', 10, 'EASY', 'public、private、protected是访问修饰符，final是最终修饰符。', 1),
('MULTIPLE', 'Java', '关于ArrayList和LinkedList说法正确的是？', 'ArrayList基于数组实现', 'LinkedList基于链表实现', 'ArrayList随机访问快', 'LinkedList插入删除快', 'ABCD', 10, 'MEDIUM', '两者各有优劣，ArrayList适合查询，LinkedList适合增删。', 1),
('JUDGE', 'Java', 'Java中int和Integer可以直接进行==比较。', '正确', '错误', NULL, NULL, 'B', 5, 'MEDIUM', 'int是基本类型，Integer是包装类，直接==比较会有自动拆箱问题，不推荐。', 1),
('JUDGE', 'Java', 'Java支持多继承。', '正确', '错误', NULL, NULL, 'B', 5, 'EASY', 'Java不支持类的多继承，但支持接口的多实现。', 1),
('SINGLE', 'Java', '下列哪个集合是线程安全的？', 'ArrayList', 'HashMap', 'Vector', 'LinkedList', 'C', 5, 'MEDIUM', 'Vector是线程安全的，但性能较差，推荐使用Collections.synchronizedList。', 1),

-- Vue题目
('SINGLE', 'Vue', 'Vue.js是什么类型的框架？', '后端框架', '渐进式前端框架', '移动端框架', '数据库框架', 'B', 5, 'EASY', 'Vue.js是一个渐进式的JavaScript框架，用于构建用户界面。', 1),
('SINGLE', 'Vue', 'Vue中用于双向数据绑定的指令是？', 'v-bind', 'v-model', 'v-if', 'v-for', 'B', 5, 'EASY', 'v-model指令用于在表单输入和应用状态之间创建双向数据绑定。', 1),
('SINGLE', 'Vue', 'Vue组件中的data必须是？', '对象', '数组', '函数', '字符串', 'C', 5, 'MEDIUM', '组件中的data必须是一个函数，返回一个数据对象，以确保每个组件实例维护独立的数据。', 1),
('MULTIPLE', 'Vue', 'Vue生命周期钩子包括？', 'created', 'mounted', 'updated', 'destroyed', 'ABCD', 10, 'MEDIUM', '这些都是Vue的生命周期钩子函数。', 1),
('JUDGE', 'Vue', 'Vue中的computed和watch都能实现盘听数据变化。', '正确', '错误', NULL, NULL, 'A', 5, 'MEDIUM', 'computed和watch都能监听数据变化，但使用场景不同。', 1),

-- 数据结构题目
('SINGLE', '数据结构', '下列哪种数据结构是线性结构？', '树', '图', '栈', '哈希表', 'C', 5, 'EASY', '栈是一种线性数据结构，遵循后进先出（LIFO）原则。', 1),
('SINGLE', '数据结构', '二叉树的遍历方式不包括？', '先序遍历', '中序遍历', '后序遍历', '随机遍历', 'D', 5, 'EASY', '二叉树的基本遍历方式包括先序、中序、后序和层次遍历。', 1),
('SINGLE', '数据结构', '快速排序的平均时间复杂度是？', 'O(n)', 'O(n log n)', 'O(n²)', 'O(log n)', 'B', 5, 'MEDIUM', '快速排序的平均时间复杂度为O(n log n)。', 1),
('MULTIPLE', '数据结构', '下列哪些是常见的排序算法？', '冒泡排序', '快速排序', '归并排序', '堆排序', 'ABCD', 10, 'EASY', '这些都是常见的排序算法。', 1),
('JUDGE', '数据结构', '哈希表的查找时间复杂度为O(1)。', '正确', '错误', NULL, NULL, 'A', 5, 'MEDIUM', '理想情况下哈希表的查找时间复杂度为O(1)。', 1),

-- 马克思主义题目
('SINGLE', '马克思主义', '马克思主义哲学的理论基础是？', '唯心主义', '辩证唯物主义', '形而上学', '经验主义', 'B', 5, 'EASY', '马克思主义哲学以辩证唯物主义和历史唯物主义为理论基础。', 1),
('SINGLE', '马克思主义', '马克思主义认为社会基本矛盾是？', '人与自然的矛盾', '生产力与生产关系的矛盾', '人与人的矛盾', '经济基础与上层建筑的矛盾', 'B', 5, 'MEDIUM', '生产力与生产关系的矛盾是社会基本矛盾。', 1),
('MULTIPLE', '马克思主义', '马克思主义的组成部分包括？', '马克思主义哲学', '马克思主义政治经济学', '科学社会主义', '以上都是', 'D', 10, 'EASY', '马克思主义由哲学、政治经济学和科学社会主义三个部分组成。', 1),
('JUDGE', '马克思主义', '实践是认识的来源。', '正确', '错误', NULL, NULL, 'A', 5, 'EASY', '马克思主义认为实践是认识的唯一来源。', 1),

-- 计算机网络题目
('SINGLE', '计算机网络', 'OSI模型共有几层？', '5层', '6层', '7层', '8层', 'C', 5, 'EASY', 'OSI参考模型共有7层。', 1),
('SINGLE', '计算机网络', 'TCP协议工作在哪一层？', '应用层', '表示层', '传输层', '网络层', 'C', 5, 'MEDIUM', 'TCP协议工作在传输层。', 1),
('MULTIPLE', '计算机网络', '下列哪些属于应用层协议？', 'HTTP', 'FTP', 'SMTP', 'TCP', 'ABC', 10, 'MEDIUM', 'HTTP、FTP、SMTP都是应用层协议，TCP是传输层协议。', 1),
('JUDGE', '计算机网络', 'IP地址是用来标识网络中主机的。', '正确', '错误', NULL, NULL, 'A', 5, 'EASY', 'IP地址用于在网络中唯一标识一台主机。', 1),

-- 操作系统题目
('SINGLE', '操作系统', '进程和线程的主要区别是？', '进程是程序的执行实例', '线程是CPU调度的基本单位', '进程拥有独立的内存空间', '以上都是', 'D', 5, 'MEDIUM', '进程和线程有多个重要区别。', 1),
('SINGLE', '操作系统', '页面置换算法中，最佳置换算法是？', 'FIFO', 'LRU', 'OPT', 'LFU', 'C', 5, 'HARD', 'OPT（最佳置换）算法理论上最优，但无法实现。', 1),
('JUDGE', '操作系统', '死锁的必要条件之一是互斥。', '正确', '错误', NULL, NULL, 'A', 5, 'MEDIUM', '死锁的四个必要条件：互斥、占有和等待、不可抢占、循环等待。', 1),

-- 数据库题目
('SINGLE', '数据库', 'SQL语言中，删除表中所有数据但保留表结构的命令是？', 'DROP', 'DELETE', 'TRUNCATE', 'REMOVE', 'C', 5, 'MEDIUM', 'TRUNCATE可以快速删除表中所有数据但保留表结构。', 1),
('SINGLE', '数据库', '数据库事务的ACID特性不包括？', '原子性', '一致性', '隔离性', '可用性', 'D', 5, 'EASY', 'ACID包括原子性、一致性、隔离性和持久性。', 1),
('MULTIPLE', '数据库', '关系型数据库的约束包括？', '主键约束', '外键约束', '唯一约束', '非空约束', 'ABCD', 10, 'MEDIUM', '这些都是常见的数据库约束。', 1),
('JUDGE', '数据库', '索引可以提高查询效率。', '正确', '错误', NULL, NULL, 'A', 5, 'EASY', '索引可以显著提高数据库的查询性能。', 1);

-- 插入示例试卷
INSERT INTO paper (paper_name, subject, total_score, duration, pass_score, description, creator_id) VALUES
('Java基础测试', 'Java', 55, 60, 33, 'Java基础知识测试，涵盖基本语法和面向对象', 1),
('Vue前端开发考试', 'Vue', 40, 45, 24, 'Vue.js框架基础知识考核，包含指令、组件、生命周期等', 1),
('数据结构与算法', '数据结构', 40, 50, 24, '数据结构基础知识，包括线性结构、树、图和排序算法', 1),
('马克思主义基本原理', '马克思主义', 35, 40, 21, '马克思主义哲学、政治经济学和科学社会主义基础知识', 1),
('计算机网络基础', '计算机网络', 35, 40, 21, '计算机网络基础知识，包括OSI模型、TCP/IP协议等', 1),
('操作系统原理', '操作系统', 20, 30, 12, '操作系统基础知识，包括进程管理、内存管理等', 1),
('数据库系统基础', '数据库', 35, 40, 21, '数据库基础知识，包括SQL语言、事务、索引等', 1);

-- 插入试卷题目关联
-- Java基础测试（试卷ID=1）
INSERT INTO paper_question (paper_id, question_id, question_order) VALUES
(1, 1, 1),  -- Java中哪个关键字可以用来定义常量
(1, 2, 2),  -- 下列哪个不是Java的基本数据类型
(1, 3, 3),  -- Java中实现多态的机制
(1, 4, 4),  -- Java中哪些是访问修饰符
(1, 5, 5),  -- 关于ArrayList和LinkedList
(1, 6, 6),  -- int和Integer比较
(1, 7, 7),  -- Java支持多继承
(1, 8, 8);  -- 线程安全的集合

-- Vue前端开发考试（试卷ID=2）
INSERT INTO paper_question (paper_id, question_id, question_order) VALUES
(2, 9, 1),   -- Vue.js是什么类型的框架
(2, 10, 2),  -- Vue中用于双向数据绑定的指令
(2, 11, 3),  -- Vue组件中的data必须是
(2, 12, 4),  -- Vue生命周期钩子
(2, 13, 5);  -- computed和watch

-- 数据结构与算法（试卷ID=3）
INSERT INTO paper_question (paper_id, question_id, question_order) VALUES
(3, 14, 1),  -- 线性结构
(3, 15, 2),  -- 二叉树的遍历方式
(3, 16, 3),  -- 快速排序的平均时间复杂度
(3, 17, 4),  -- 常见的排序算法
(3, 18, 5);  -- 哈希表的查找时间复杂度

-- 马克思主义基本原理（试卷ID=4）
INSERT INTO paper_question (paper_id, question_id, question_order) VALUES
(4, 19, 1),  -- 马克思主义哲学的理论基础
(4, 20, 2),  -- 社会基本矛盾
(4, 21, 3),  -- 马克思主义的组成部分
(4, 22, 4);  -- 实践是认识的来源

-- 计算机网络基础（试卷ID=5）
INSERT INTO paper_question (paper_id, question_id, question_order) VALUES
(5, 23, 1),  -- OSI模型层数
(5, 24, 2),  -- TCP协议工作层
(5, 25, 3),  -- 应用层协议
(5, 26, 4);  -- IP地址

-- 操作系统原理（试卷ID=6）
INSERT INTO paper_question (paper_id, question_id, question_order) VALUES
(6, 27, 1),  -- 进程和线程的主要区别
(6, 28, 2),  -- 最佳置换算法
(6, 29, 3);  -- 死锁的必要条件

-- 数据库系统基础（试卷ID=7）
INSERT INTO paper_question (paper_id, question_id, question_order) VALUES
(7, 30, 1),  -- TRUNCATE命令
(7, 31, 2),  -- ACID特性
(7, 32, 3),  -- 数据库约束
(7, 33, 4);  -- 索引可以提高查询效率

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
