-- ========================================
-- 性能优化SQL脚本
-- 添加索引以提升查询性能
-- ========================================

USE exam_system;

-- 1. exam_record表优化索引
-- 已有索引: idx_student_id, idx_paper_id, idx_status
-- 添加复合索引以优化学生考试记录查询
CREATE INDEX idx_student_status_time ON exam_record(student_id, status, create_time DESC);

-- 2. answer_record表优化索引
-- 已有索引: idx_record_id, idx_question_id
-- 添加复合索引以优化答题正确性统计
CREATE INDEX idx_record_correct ON answer_record(record_id, is_correct);

-- 3. paper_question表优化索引
-- 添加索引以优化试卷题目查询
CREATE INDEX idx_paper_order ON paper_question(paper_id, question_order);

-- 4. question表优化索引
-- 已有索引: idx_question_type, idx_subject, idx_difficulty
-- 添加复合索引以优化多条件查询
CREATE INDEX idx_subject_type_difficulty ON question(subject, question_type, difficulty);

-- ========================================
-- 查看已创建的索引
-- ========================================
SHOW INDEX FROM exam_record;
SHOW INDEX FROM answer_record;
SHOW INDEX FROM paper_question;
SHOW INDEX FROM question;

-- ========================================
-- 性能分析示例（可用于测试查询性能）
-- ========================================
-- 分析学生考试记录查询
EXPLAIN SELECT er.*, 
       p.paper_id as p_paper_id, p.paper_name, p.subject, p.total_score, 
       p.duration, p.pass_score, p.description, p.is_published, 
       p.creator_id as p_creator_id, p.create_time as p_create_time, 
       p.update_time as p_update_time 
FROM exam_record er 
LEFT JOIN paper p ON er.paper_id = p.paper_id 
WHERE er.student_id = 1 
ORDER BY er.create_time DESC;

-- 分析批量答题记录查询
EXPLAIN SELECT * FROM answer_record WHERE record_id IN (1, 2, 3, 4, 5);
