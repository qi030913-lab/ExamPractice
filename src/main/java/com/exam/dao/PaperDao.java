package com.exam.dao;

import com.exam.exception.DatabaseException;
import com.exam.model.Paper;
import com.exam.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 试卷数据访问对象
 */
public class PaperDao {

    /**
     * 根据ID查询试卷
     */
    public Paper findById(Integer paperId) {
        String sql = "SELECT * FROM paper WHERE paper_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, paperId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractPaper(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询试卷失败", e);
        }
        return null;
    }

    /**
     * 根据试卷名称查询试卷
     */
    public Paper findByName(String paperName) {
        String sql = "SELECT * FROM paper WHERE paper_name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, paperName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractPaper(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询试卷失败", e);
        }
        return null;
    }

    /**
     * 查询所有试卷
     */
    public List<Paper> findAll() {
        String sql = "SELECT * FROM paper ORDER BY paper_id DESC";
        List<Paper> papers = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                papers.add(extractPaper(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询试卷列表失败", e);
        }
        return papers;
    }

    /**
     * 查询所有已发布的试卷（学生端使用）
     */
    public List<Paper> findAllPublished() {
        String sql = "SELECT * FROM paper WHERE is_published = TRUE ORDER BY paper_id DESC";
        List<Paper> papers = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                papers.add(extractPaper(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询已发布试卷列表失败", e);
        }
        return papers;
    }

    /**
     * 查询所有已发布的试卷及题型统计（性能优化版本）
     * 使用单条SQL查询试卷和题型统计，避免N+1问题
     * @return 试卷列表，每个试卷包含题型统计信息
     */
    public List<Paper> findAllPublishedWithQuestionStats() {
        String sql = "SELECT p.*, " +
                     "COUNT(CASE WHEN q.question_type = 'SINGLE' THEN 1 END) AS single_count, " +
                     "COUNT(CASE WHEN q.question_type = 'MULTIPLE' THEN 1 END) AS multiple_count, " +
                     "COUNT(CASE WHEN q.question_type = 'JUDGE' THEN 1 END) AS judge_count, " +
                     "COUNT(CASE WHEN q.question_type = 'BLANK' THEN 1 END) AS blank_count " +
                     "FROM paper p " +
                     "LEFT JOIN paper_question pq ON p.paper_id = pq.paper_id " +
                     "LEFT JOIN question q ON pq.question_id = q.question_id " +
                     "WHERE p.is_published = TRUE " +
                     "GROUP BY p.paper_id " +
                     "ORDER BY p.paper_id DESC";
        List<Paper> papers = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Paper paper = extractPaper(rs);
                // 存储题型统计到临时属性
                paper.setSingleCount(rs.getInt("single_count"));
                paper.setMultipleCount(rs.getInt("multiple_count"));
                paper.setJudgeCount(rs.getInt("judge_count"));
                paper.setBlankCount(rs.getInt("blank_count"));
                papers.add(paper);
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询已发布试卷列表失败", e);
        }
        return papers;
    }

    /**
     * 添加试卷
     */
    public int insert(Paper paper) {
        String sql = "INSERT INTO paper (paper_name, subject, total_score, duration, pass_score, description, creator_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, paper.getPaperName());
            pstmt.setString(2, paper.getSubject());
            pstmt.setInt(3, paper.getTotalScore());
            pstmt.setInt(4, paper.getDuration());
            pstmt.setInt(5, paper.getPassScore());
            pstmt.setString(6, paper.getDescription());
            
            if (paper.getCreatorId() != null) {
                pstmt.setInt(7, paper.getCreatorId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
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
            throw new DatabaseException("添加试卷失败", e);
        }
        return 0;
    }

    /**
     * 添加试卷题目关联
     */
    public void addPaperQuestion(Integer paperId, Integer questionId, Integer order) {
        // 使用 INSERT IGNORE 忽略重复的题目
        String sql = "INSERT IGNORE INTO paper_question (paper_id, question_id, question_order) VALUES (?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, paperId);
            pstmt.setInt(2, questionId);
            pstmt.setInt(3, order);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("添加试卷题目失败", e);
        }
    }

    /**
     * 删除试卷的所有题目
     */
    public void deletePaperQuestions(Integer paperId) {
        String sql = "DELETE FROM paper_question WHERE paper_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, paperId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("删除试卷题目失败", e);
        }
    }

    /**
     * 查询使用了指定题目的试卷列表（性能优化版本）
     * @param questionId 题目 ID
     * @return 使用了该题目的试卷列表
     */
    public List<Paper> findPapersUsingQuestion(Integer questionId) {
        String sql = "SELECT DISTINCT p.* FROM paper p " +
                     "INNER JOIN paper_question pq ON p.paper_id = pq.paper_id " +
                     "WHERE pq.question_id = ?";
        List<Paper> papers = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, questionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    papers.add(extractPaper(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询使用题目的试卷列表失败", e);
        }
        return papers;
    }

    /**
     * 更新试卷
     */
    public int update(Paper paper) {
        String sql = "UPDATE paper SET paper_name = ?, subject = ?, total_score = ?, " +
                     "duration = ?, pass_score = ?, description = ? WHERE paper_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, paper.getPaperName());
            pstmt.setString(2, paper.getSubject());
            pstmt.setInt(3, paper.getTotalScore());
            pstmt.setInt(4, paper.getDuration());
            pstmt.setInt(5, paper.getPassScore());
            pstmt.setString(6, paper.getDescription());
            pstmt.setInt(7, paper.getPaperId());
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("更新试卷失败", e);
        }
    }

    /**
     * 更新试卷发布状态
     */
    public int updatePublishStatus(Integer paperId, Boolean isPublished) {
        String sql = "UPDATE paper SET is_published = ? WHERE paper_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, isPublished);
            pstmt.setInt(2, paperId);
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("更新试卷发布状态失败", e);
        }
    }

    /**
     * 删除试卷
     */
    public int delete(Integer paperId) {
        String sql = "DELETE FROM paper WHERE paper_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, paperId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("删除试卷失败", e);
        }
    }

    /**
     * 查询所有试卷及题目数量（性能优化版本，用于教师端试卷管理）
     * 使用单条SQL查询，避免N+1问题
     * @return 试卷列表，每个试卷包含题目数量
     */
    public List<Paper> findAllWithQuestionCount() {
        String sql = "SELECT p.*, COUNT(pq.question_id) AS question_count " +
                     "FROM paper p " +
                     "LEFT JOIN paper_question pq ON p.paper_id = pq.paper_id " +
                     "GROUP BY p.paper_id " +
                     "ORDER BY p.paper_id DESC";
        List<Paper> papers = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Paper paper = extractPaper(rs);
                // 使用 singleCount 临时存储题目总数（复用现有字段）
                paper.setSingleCount(rs.getInt("question_count"));
                papers.add(paper);
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询试卷列表失败", e);
        }
        return papers;
    }

    /**
     * 从ResultSet提取Paper对象
     */
    private Paper extractPaper(ResultSet rs) throws SQLException {
        Paper paper = new Paper();
        paper.setPaperId(rs.getInt("paper_id"));
        paper.setPaperName(rs.getString("paper_name"));
        paper.setSubject(rs.getString("subject"));
        paper.setTotalScore(rs.getInt("total_score"));
        paper.setDuration(rs.getInt("duration"));
        paper.setPassScore(rs.getInt("pass_score"));
        paper.setDescription(rs.getString("description"));
        paper.setIsPublished(rs.getBoolean("is_published"));
        paper.setCreatorId(rs.getInt("creator_id"));
        
        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            paper.setCreateTime(createTime.toLocalDateTime());
        }
        
        Timestamp updateTime = rs.getTimestamp("update_time");
        if (updateTime != null) {
            paper.setUpdateTime(updateTime.toLocalDateTime());
        }
        
        return paper;
    }
}
