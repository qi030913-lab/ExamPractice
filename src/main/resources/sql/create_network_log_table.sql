-- ========================================
-- 网络通信日志表
-- 用于保存学生端与教师端的通信记录
-- ========================================

USE exam_system;

-- 删除旧表（如果存在）
DROP TABLE IF EXISTS network_log;

-- 创建网络通信日志表
CREATE TABLE network_log (
    log_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    student_id INT NOT NULL COMMENT '学生ID',
    message_type VARCHAR(20) NOT NULL COMMENT '消息类型：SEND(发送)/RECEIVE(接收)/SYSTEM(系统)',
    message_content TEXT NOT NULL COMMENT '消息内容',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_student_id (student_id),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (student_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网络通信日志表';
