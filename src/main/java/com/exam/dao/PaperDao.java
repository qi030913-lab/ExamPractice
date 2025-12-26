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
        String sql = "INSERT INTO paper_question (paper_id, question_id, question_order) VALUES (?, ?, ?)";
        
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
