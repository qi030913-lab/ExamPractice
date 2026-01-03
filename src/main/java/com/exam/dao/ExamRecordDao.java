package com.exam.dao;

import com.exam.exception.DatabaseException;
import com.exam.model.AnswerRecord;
import com.exam.model.ExamRecord;
import com.exam.model.enums.ExamStatus;
import com.exam.util.DBUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 考试记录数据访问对象
 */
public class ExamRecordDao {

    /**
     * 根据ID查询考试记录
     */
    public ExamRecord findById(Integer recordId) {
        String sql = "SELECT * FROM exam_record WHERE record_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, recordId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractExamRecord(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询考试记录失败", e);
        }
        return null;
    }

    /**
     * 根据学生ID查询考试记录
     */
    public List<ExamRecord> findByStudentId(Integer studentId) {
        String sql = "SELECT * FROM exam_record WHERE student_id = ? ORDER BY create_time DESC";
        List<ExamRecord> records = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(extractExamRecord(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询考试记录失败", e);
        }
        return records;
    }

    /**
     * 根据学生ID查询考试记录总数
     */
    public int countByStudentId(Integer studentId) {
        String sql = "SELECT COUNT(*) FROM exam_record WHERE student_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询考试记录总数失败", e);
        }
        return 0;
    }

    /**
     * 根据学生ID查询考试记录（包含试卷信息）- 性能优化版本
     * 使用LEFT JOIN一次性查询，避免N+1问题
     */
    public List<ExamRecord> findByStudentIdWithPaper(Integer studentId) {
        String sql = "SELECT er.*, " +
                     "p.paper_id as p_paper_id, p.paper_name, p.subject, p.total_score, " +
                     "p.duration, p.pass_score, p.description, p.is_published, " +
                     "p.creator_id as p_creator_id, p.create_time as p_create_time, " +
                     "p.update_time as p_update_time " +
                     "FROM exam_record er " +
                     "LEFT JOIN paper p ON er.paper_id = p.paper_id " +
                     "WHERE er.student_id = ? ORDER BY er.create_time DESC";
        List<ExamRecord> records = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ExamRecord record = extractExamRecord(rs);
                    // 提取试卷信息
                    if (rs.getObject("p_paper_id") != null) {
                        com.exam.model.Paper paper = new com.exam.model.Paper();
                        paper.setPaperId(rs.getInt("p_paper_id"));
                        paper.setPaperName(rs.getString("paper_name"));
                        paper.setSubject(rs.getString("subject"));
                        paper.setTotalScore(rs.getInt("total_score"));
                        paper.setDuration(rs.getInt("duration"));
                        paper.setPassScore(rs.getInt("pass_score"));
                        paper.setDescription(rs.getString("description"));
                        paper.setIsPublished(rs.getBoolean("is_published"));
                        paper.setCreatorId(rs.getInt("p_creator_id"));
                        
                        Timestamp pCreateTime = rs.getTimestamp("p_create_time");
                        if (pCreateTime != null) {
                            paper.setCreateTime(pCreateTime.toLocalDateTime());
                        }
                        
                        Timestamp pUpdateTime = rs.getTimestamp("p_update_time");
                        if (pUpdateTime != null) {
                            paper.setUpdateTime(pUpdateTime.toLocalDateTime());
                        }
                        
                        record.setPaper(paper);
                    }
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询考试记录失败", e);
        }
        return records;
    }

    /**
     * 根据学生ID分页查询考试记录（包含试卷信息）- 性能优化版本
     * @param studentId 学生ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 考试记录列表
     */
    public List<ExamRecord> findByStudentIdWithPaperPaginated(Integer studentId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        String sql = "SELECT er.*, " +
                     "p.paper_id as p_paper_id, p.paper_name, p.subject, p.total_score, " +
                     "p.duration, p.pass_score, p.description, p.is_published, " +
                     "p.creator_id as p_creator_id, p.create_time as p_create_time, " +
                     "p.update_time as p_update_time " +
                     "FROM exam_record er " +
                     "LEFT JOIN paper p ON er.paper_id = p.paper_id " +
                     "WHERE er.student_id = ? ORDER BY er.create_time DESC " +
                     "LIMIT ? OFFSET ?";
        List<ExamRecord> records = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ExamRecord record = extractExamRecord(rs);
                    // 提取试卷信息
                    if (rs.getObject("p_paper_id") != null) {
                        com.exam.model.Paper paper = new com.exam.model.Paper();
                        paper.setPaperId(rs.getInt("p_paper_id"));
                        paper.setPaperName(rs.getString("paper_name"));
                        paper.setSubject(rs.getString("subject"));
                        paper.setTotalScore(rs.getInt("total_score"));
                        paper.setDuration(rs.getInt("duration"));
                        paper.setPassScore(rs.getInt("pass_score"));
                        paper.setDescription(rs.getString("description"));
                        paper.setIsPublished(rs.getBoolean("is_published"));
                        paper.setCreatorId(rs.getInt("p_creator_id"));
                        
                        Timestamp pCreateTime = rs.getTimestamp("p_create_time");
                        if (pCreateTime != null) {
                            paper.setCreateTime(pCreateTime.toLocalDateTime());
                        }
                        
                        Timestamp pUpdateTime = rs.getTimestamp("p_update_time");
                        if (pUpdateTime != null) {
                            paper.setUpdateTime(pUpdateTime.toLocalDateTime());
                        }
                        
                        record.setPaper(paper);
                    }
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("分页查询考试记录失败", e);
        }
        return records;
    }

    /**
     * 根据试卷ID查询所有考试记录
     */
    public List<ExamRecord> findByPaperId(Integer paperId) {
        String sql = "SELECT * FROM exam_record WHERE paper_id = ? ORDER BY create_time DESC";
        List<ExamRecord> records = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, paperId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(extractExamRecord(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询考试记录失败", e);
        }
        return records;
    }

    /**
     * 添加考试记录
     */
    public int insert(ExamRecord record) {
        String sql = "INSERT INTO exam_record (student_id, paper_id, start_time, status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, record.getStudentId());
            pstmt.setInt(2, record.getPaperId());
            
            if (record.getStartTime() != null) {
                pstmt.setTimestamp(3, Timestamp.valueOf(record.getStartTime()));
            } else {
                pstmt.setNull(3, Types.TIMESTAMP);
            }
            
            pstmt.setString(4, record.getStatus().name());
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("添加考试记录失败", e);
        }
        return 0;
    }

    /**
     * 更新考试记录
     */
    public int update(ExamRecord record) {
        String sql = "UPDATE exam_record SET start_time = ?, end_time = ?, submit_time = ?, score = ?, status = ? WHERE record_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (record.getStartTime() != null) {
                pstmt.setTimestamp(1, Timestamp.valueOf(record.getStartTime()));
            } else {
                pstmt.setNull(1, Types.TIMESTAMP);
            }
            
            if (record.getEndTime() != null) {
                pstmt.setTimestamp(2, Timestamp.valueOf(record.getEndTime()));
            } else {
                pstmt.setNull(2, Types.TIMESTAMP);
            }
            
            if (record.getSubmitTime() != null) {
                pstmt.setTimestamp(3, Timestamp.valueOf(record.getSubmitTime()));
            } else {
                pstmt.setNull(3, Types.TIMESTAMP);
            }
            
            if (record.getScore() != null) {
                pstmt.setBigDecimal(4, record.getScore());
            } else {
                pstmt.setNull(4, Types.DECIMAL);
            }
            
            pstmt.setString(5, record.getStatus().name());
            pstmt.setInt(6, record.getRecordId());
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("更新考试记录失败", e);
        }
    }

    /**
     * 添加答题记录
     */
    public void insertAnswerRecord(AnswerRecord answerRecord) {
        String sql = "INSERT INTO answer_record (record_id, question_id, student_answer, is_correct, score) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, answerRecord.getRecordId());
            pstmt.setInt(2, answerRecord.getQuestionId());
            pstmt.setString(3, answerRecord.getStudentAnswer());
            
            if (answerRecord.getIsCorrect() != null) {
                pstmt.setBoolean(4, answerRecord.getIsCorrect());
            } else {
                pstmt.setNull(4, Types.BOOLEAN);
            }
            
            if (answerRecord.getScore() != null) {
                pstmt.setBigDecimal(5, answerRecord.getScore());
            } else {
                pstmt.setNull(5, Types.DECIMAL);
            }
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("添加答题记录失败", e);
        }
    }

    /**
     * 批量添加答题记录 - 性能优化版本
     * 使用批量插入提升性能，减少数据库往返次数
     */
    public void insertAnswerRecordsBatch(List<AnswerRecord> answerRecords) {
        if (answerRecords == null || answerRecords.isEmpty()) {
            return;
        }
        
        String sql = "INSERT INTO answer_record (record_id, question_id, student_answer, is_correct, score) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置为非自动提交，提升批量插入性能
            conn.setAutoCommit(false);
            
            for (AnswerRecord answerRecord : answerRecords) {
                pstmt.setInt(1, answerRecord.getRecordId());
                pstmt.setInt(2, answerRecord.getQuestionId());
                pstmt.setString(3, answerRecord.getStudentAnswer());
                
                if (answerRecord.getIsCorrect() != null) {
                    pstmt.setBoolean(4, answerRecord.getIsCorrect());
                } else {
                    pstmt.setNull(4, Types.BOOLEAN);
                }
                
                if (answerRecord.getScore() != null) {
                    pstmt.setBigDecimal(5, answerRecord.getScore());
                } else {
                    pstmt.setNull(5, Types.DECIMAL);
                }
                
                pstmt.addBatch();
            }
            
            // 执行批量插入
            pstmt.executeBatch();
            conn.commit();
            
        } catch (SQLException e) {
            throw new DatabaseException("批量添加答题记录失败", e);
        }
    }

    /**
     * 查询答题记录
     */
    public List<AnswerRecord> findAnswerRecords(Integer recordId) {
        String sql = "SELECT * FROM answer_record WHERE record_id = ?";
        List<AnswerRecord> answers = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, recordId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AnswerRecord answer = new AnswerRecord();
                    answer.setAnswerId(rs.getInt("answer_id"));
                    answer.setRecordId(rs.getInt("record_id"));
                    answer.setQuestionId(rs.getInt("question_id"));
                    answer.setStudentAnswer(rs.getString("student_answer"));
                    answer.setIsCorrect(rs.getBoolean("is_correct"));
                    
                    BigDecimal score = rs.getBigDecimal("score");
                    if (score != null) {
                        answer.setScore(score);
                    }
                    
                    answers.add(answer);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询答题记录失败", e);
        }
        return answers;
    }

    /**
     * 批量查询多个考试记录的答题记录 - 性能优化版本
     * 使用IN查询一次性获取，避免多次查询
     */
    public java.util.Map<Integer, List<AnswerRecord>> findAnswerRecordsByRecordIds(List<Integer> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        
        // 构建IN查询的占位符
        String placeholders = String.join(",", java.util.Collections.nCopies(recordIds.size(), "?"));
        String sql = "SELECT * FROM answer_record WHERE record_id IN (" + placeholders + ")";
        
        java.util.Map<Integer, List<AnswerRecord>> resultMap = new java.util.HashMap<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置参数
            for (int i = 0; i < recordIds.size(); i++) {
                pstmt.setInt(i + 1, recordIds.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AnswerRecord answer = new AnswerRecord();
                    answer.setAnswerId(rs.getInt("answer_id"));
                    answer.setRecordId(rs.getInt("record_id"));
                    answer.setQuestionId(rs.getInt("question_id"));
                    answer.setStudentAnswer(rs.getString("student_answer"));
                    answer.setIsCorrect(rs.getBoolean("is_correct"));
                    
                    BigDecimal score = rs.getBigDecimal("score");
                    if (score != null) {
                        answer.setScore(score);
                    }
                    
                    // 按record_id分组
                    resultMap.computeIfAbsent(answer.getRecordId(), k -> new ArrayList<>()).add(answer);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("批量查询答题记录失败", e);
        }
        return resultMap;
    }

    /**
     * 从ResultSet提取ExamRecord对象
     */
    private ExamRecord extractExamRecord(ResultSet rs) throws SQLException {
        ExamRecord record = new ExamRecord();
        record.setRecordId(rs.getInt("record_id"));
        record.setStudentId(rs.getInt("student_id"));
        record.setPaperId(rs.getInt("paper_id"));
        
        Timestamp startTime = rs.getTimestamp("start_time");
        if (startTime != null) {
            record.setStartTime(startTime.toLocalDateTime());
        }
        
        Timestamp endTime = rs.getTimestamp("end_time");
        if (endTime != null) {
            record.setEndTime(endTime.toLocalDateTime());
        }
        
        Timestamp submitTime = rs.getTimestamp("submit_time");
        if (submitTime != null) {
            record.setSubmitTime(submitTime.toLocalDateTime());
        }
        
        BigDecimal score = rs.getBigDecimal("score");
        if (score != null) {
            record.setScore(score);
        }
        
        record.setStatus(ExamStatus.valueOf(rs.getString("status")));
        
        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            record.setCreateTime(createTime.toLocalDateTime());
        }
        
        return record;
    }
}
