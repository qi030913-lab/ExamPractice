package com.exam.dao;


import com.exam.exception.DatabaseException;
import com.exam.model.Question;
import com.exam.model.enums.Difficulty;
import com.exam.model.enums.QuestionType;
import com.exam.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目数据访问对象
 */
public class QuestionDao {

    /**
     * 根据ID查询题目
     */
    public Question findById(Integer questionId) {
        String sql = "SELECT * FROM question WHERE question_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, questionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractQuestion(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询题目失败", e);
        }
        return null;
    }

    /**
     * 批量查询题目（性能优化版本）
     * @param questionIds 题目ID列表
     * @return 题目ID到题目对象的映射
     */
    public java.util.Map<Integer, Question> findByIds(java.util.Collection<Integer> questionIds) {
        java.util.Map<Integer, Question> resultMap = new java.util.HashMap<>();
        
        if (questionIds == null || questionIds.isEmpty()) {
            return resultMap;
        }
        
        // 构建IN查询的占位符
        String placeholders = String.join(",", java.util.Collections.nCopies(questionIds.size(), "?"));
        String sql = "SELECT * FROM question WHERE question_id IN (" + placeholders + ")";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置参数
            int index = 1;
            for (Integer questionId : questionIds) {
                pstmt.setInt(index++, questionId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Question question = extractQuestion(rs);
                    resultMap.put(question.getQuestionId(), question);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("批量查询题目失败", e);
        }
        
        return resultMap;
    }

    /**
     * 查询所有题目
     */
    public List<Question> findAll() {
        String sql = "SELECT * FROM question ORDER BY question_id";
        List<Question> questions = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                questions.add(extractQuestion(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询题目列表失败", e);
        }
        return questions;
    }

    /**
     * 根据科目查询题目
     */
    public List<Question> findBySubject(String subject) {
        String sql = "SELECT * FROM question WHERE subject = ? ORDER BY question_id";
        List<Question> questions = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, subject);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(extractQuestion(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询题目列表失败", e);
        }
        return questions;
    }

    /**
     * 根据试卷ID查询题目列表
     */
    public List<Question> findByPaperId(Integer paperId) {
        String sql = "SELECT q.* FROM question q " +
                     "INNER JOIN paper_question pq ON q.question_id = pq.question_id " +
                     "WHERE pq.paper_id = ? ORDER BY pq.question_order";
        List<Question> questions = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, paperId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(extractQuestion(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询试卷题目失败", e);
        }
        return questions;
    }

    /**
     * 添加题目
     */
    public int insert(Question question) {
        String sql = "INSERT INTO question (question_type, subject, content, option_a, option_b, " +
                     "option_c, option_d, correct_answer, score, difficulty, analysis, creator_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, question.getQuestionType().name());
            pstmt.setString(2, question.getSubject());
            pstmt.setString(3, question.getContent());
            pstmt.setString(4, question.getOptionA());
            pstmt.setString(5, question.getOptionB());
            pstmt.setString(6, question.getOptionC());
            pstmt.setString(7, question.getOptionD());
            pstmt.setString(8, question.getCorrectAnswer());
            pstmt.setInt(9, question.getScore());
            pstmt.setString(10, question.getDifficulty() != null ? question.getDifficulty().name() : "MEDIUM");
            pstmt.setString(11, question.getAnalysis());
            
            if (question.getCreatorId() != null) {
                pstmt.setInt(12, question.getCreatorId());
            } else {
                pstmt.setNull(12, Types.INTEGER);
            }
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("添加题目失败", e);
        }
        return 0;
    }

    /**
     * 更新题目
     */
    public int update(Question question) {
        String sql = "UPDATE question SET question_type = ?, subject = ?, content = ?, " +
                     "option_a = ?, option_b = ?, option_c = ?, option_d = ?, " +
                     "correct_answer = ?, score = ?, difficulty = ?, analysis = ? " +
                     "WHERE question_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, question.getQuestionType().name());
            pstmt.setString(2, question.getSubject());
            pstmt.setString(3, question.getContent());
            pstmt.setString(4, question.getOptionA());
            pstmt.setString(5, question.getOptionB());
            pstmt.setString(6, question.getOptionC());
            pstmt.setString(7, question.getOptionD());
            pstmt.setString(8, question.getCorrectAnswer());
            pstmt.setInt(9, question.getScore());
            pstmt.setString(10, question.getDifficulty().name());
            pstmt.setString(11, question.getAnalysis());
            pstmt.setInt(12, question.getQuestionId());
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("更新题目失败", e);
        }
    }

    /**
     * 删除题目
     */
    public int delete(Integer questionId) {
        String sql = "DELETE FROM question WHERE question_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, questionId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("删除题目失败", e);
        }
    }

    /**
     * 从ResultSet提取Question对象
     */
    private Question extractQuestion(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setQuestionId(rs.getInt("question_id"));
        question.setQuestionType(QuestionType.valueOf(rs.getString("question_type")));
        question.setSubject(rs.getString("subject"));
        question.setContent(rs.getString("content"));
        question.setOptionA(rs.getString("option_a"));
        question.setOptionB(rs.getString("option_b"));
        question.setOptionC(rs.getString("option_c"));
        question.setOptionD(rs.getString("option_d"));
        question.setCorrectAnswer(rs.getString("correct_answer"));
        question.setScore(rs.getInt("score"));
        
        String difficulty = rs.getString("difficulty");
        if (difficulty != null) {
            question.setDifficulty(Difficulty.valueOf(difficulty));
        }
        
        question.setAnalysis(rs.getString("analysis"));
        question.setCreatorId(rs.getInt("creator_id"));
        
        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            question.setCreateTime(createTime.toLocalDateTime());
        }
        
        Timestamp updateTime = rs.getTimestamp("update_time");
        if (updateTime != null) {
            question.setUpdateTime(updateTime.toLocalDateTime());
        }
        
        return question;
    }
    
    /**
     * 搜索题目
     * @param content 题目内容关键词
     * @param subject 科目
     * @param type 题目类型
     * @param difficulty 难度
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 题目列表
     */
    public List<Question> search(String content, String subject, com.exam.model.enums.QuestionType type, com.exam.model.enums.Difficulty difficulty, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM question WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (content != null && !content.trim().isEmpty()) {
            sql.append(" AND content LIKE ?");
            params.add("%" + content + "%");
        }
        
        if (subject != null && !subject.trim().isEmpty()) {
            sql.append(" AND subject = ?");
            params.add(subject);
        }
        
        if (type != null) {
            sql.append(" AND question_type = ?");
            params.add(type.name());
        }
        
        if (difficulty != null) {
            sql.append(" AND difficulty = ?");
            params.add(difficulty.name());
        }
        
        sql.append(" ORDER BY question_id LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        
        List<Question> questions = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(extractQuestion(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("搜索题目失败", e);
        }
        
        return questions;
    }
    
    /**
     * 统计符合条件的题目总数（性能优化版本）
     * @param content 题目内容关键词
     * @param subject 科目
     * @param type 题目类型
     * @param difficulty 难度
     * @return 题目总数
     */
    public int countQuestions(String content, String subject, com.exam.model.enums.QuestionType type, com.exam.model.enums.Difficulty difficulty) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM question WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (content != null && !content.trim().isEmpty()) {
            sql.append(" AND content LIKE ?");
            params.add("%" + content + "%");
        }
        
        if (subject != null && !subject.trim().isEmpty()) {
            sql.append(" AND subject = ?");
            params.add(subject);
        }
        
        if (type != null) {
            sql.append(" AND question_type = ?");
            params.add(type.name());
        }
        
        if (difficulty != null) {
            sql.append(" AND difficulty = ?");
            params.add(difficulty.name());
        }
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("统计题目总数失败", e);
        }
        
        return 0;
    }
}
