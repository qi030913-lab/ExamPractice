package com.exam.dao;

import com.exam.exception.DatabaseException;
import com.exam.model.User;
import com.exam.model.enums.UserRole;
import com.exam.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问对象
 * 实现对用户表的CRUD操作
 */
public class UserDao {

    /**
     * 根据姓名、学号和密码查询用户（登录验证）
     */
    public User findByNameNumberAndPassword(String realName, String studentNumber, String password) {
        String sql = "SELECT * FROM user WHERE real_name = ? AND student_number = ? AND password = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, realName);
            pstmt.setString(2, studentNumber);
            pstmt.setString(3, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询用户失败", e);
        }
        return null;
    }

    /**
     * 根据姓名和密码查询用户（教师登录）
     */
    public User findByNameAndPassword(String realName, String password) {
        String sql = "SELECT * FROM user WHERE real_name = ? AND password = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, realName);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询用户失败", e);
        }
        return null;
    }

    /**
     * 根据用户ID查询用户
     */
    public User findById(Integer userId) {
        String sql = "SELECT * FROM user WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询用户失败", e);
        }
        return null;
    }

    /**
     * 根据学号查询用户
     */
    public User findByStudentNumber(String studentNumber) {
        String sql = "SELECT * FROM user WHERE student_number = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, studentNumber);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询用户失败", e);
        }
        return null;
    }

    /**
     * 查询所有学生
     */
    public List<User> findAllStudents() {
        String sql = "SELECT * FROM user WHERE role = 'STUDENT' ORDER BY user_id";
        List<User> students = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                students.add(extractUser(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("查询学生列表失败", e);
        }
        return students;
    }

    /**
     * 添加用户
     */
    public int insert(User user) {
        String sql = "INSERT INTO user (real_name, student_number, password, role, email, phone, gender, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getRealName());
            pstmt.setString(2, user.getStudentNumber());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getRole().name());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPhone());
            pstmt.setString(7, user.getGender());
            pstmt.setString(8, user.getStatus() != null ? user.getStatus() : "ACTIVE");
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("添加用户失败", e);
        }
        return 0;
    }

    /**
     * 更新用户信息
     */
    public int update(User user) {
        String sql = "UPDATE user SET password = ?, real_name = ?, email = ?, phone = ? WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getPassword());
            pstmt.setString(2, user.getRealName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPhone());
            pstmt.setInt(5, user.getUserId());
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("更新用户失败", e);
        }
    }

    /**
     * 删除用户
     */
    public int delete(Integer userId) {
        String sql = "DELETE FROM user WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("删除用户失败", e);
        }
    }

    /**
     * 从ResultSet提取User对象
     */
    private User extractUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setRealName(rs.getString("real_name"));
        user.setStudentNumber(rs.getString("student_number"));
        user.setPassword(rs.getString("password"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setGender(rs.getString("gender"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setStatus(rs.getString("status"));
        
        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            user.setCreateTime(createTime.toLocalDateTime());
        }
        
        Timestamp updateTime = rs.getTimestamp("update_time");
        if (updateTime != null) {
            user.setUpdateTime(updateTime.toLocalDateTime());
        }
        
        Timestamp lastLoginTime = rs.getTimestamp("last_login_time");
        if (lastLoginTime != null) {
            user.setLastLoginTime(lastLoginTime.toLocalDateTime());
        }
        
        return user;
    }
}
