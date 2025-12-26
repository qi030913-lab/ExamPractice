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
