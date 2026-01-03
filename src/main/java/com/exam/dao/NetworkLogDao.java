package com.exam.dao;

import com.exam.exception.DatabaseException;
import com.exam.model.NetworkLog;
import com.exam.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 网络通信日志数据访问对象
 * 实现对通信日志表的CRUD操作
 * 
 * @author 在线考试系统开发组
 * @version 1.0
 */
public class NetworkLogDao {

    /**
     * 保存一条通信日志
     */
    public int insert(NetworkLog log) {
        String sql = "INSERT INTO network_log (student_id, message_type, message_content) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, log.getStudentId());
            pstmt.setString(2, log.getMessageType());
            pstmt.setString(3, log.getMessageContent());
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("保存通信日志失败", e);
        }
        return 0;
    }

    /**
     * 查询指定学生的所有通信日志，按时间升序排列
     */
    public List<NetworkLog> findByStudentId(Integer studentId) {
        String sql = "SELECT * FROM network_log WHERE student_id = ? ORDER BY create_time ASC";
        List<NetworkLog> logs = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(extractNetworkLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询通信日志失败", e);
        }
        return logs;
    }

    /**
     * 查询指定学生的最近N条通信日志
     */
    public List<NetworkLog> findRecentByStudentId(Integer studentId, int limit) {
        String sql = "SELECT * FROM network_log WHERE student_id = ? ORDER BY create_time DESC LIMIT ?";
        List<NetworkLog> logs = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(extractNetworkLog(rs));
                }
            }
            // 反转列表，使其按时间升序
            java.util.Collections.reverse(logs);
        } catch (SQLException e) {
            throw new DatabaseException("查询最近通信日志失败", e);
        }
        return logs;
    }

    /**
     * 删除指定学生的所有通信日志
     */
    public int deleteByStudentId(Integer studentId) {
        String sql = "DELETE FROM network_log WHERE student_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("删除通信日志失败", e);
        }
    }

    /**
     * 从ResultSet提取NetworkLog对象
     */
    private NetworkLog extractNetworkLog(ResultSet rs) throws SQLException {
        NetworkLog log = new NetworkLog();
        log.setLogId(rs.getInt("log_id"));
        log.setStudentId(rs.getInt("student_id"));
        log.setMessageType(rs.getString("message_type"));
        log.setMessageContent(rs.getString("message_content"));
        
        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            log.setCreateTime(createTime.toLocalDateTime());
        }
        
        return log;
    }
}
