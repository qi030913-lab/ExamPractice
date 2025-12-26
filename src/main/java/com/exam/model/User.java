package com.exam.model;

import com.exam.model.enums.UserRole;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户实体类
 * 使用封装、继承等面向对象特性
 */
public class User {
    private Integer userId;
    private String realName;  // 真实姓名
    private String studentNumber;  // 学号（唯一标识）
    private String password;
    private UserRole role;
    private String email;
    private String phone;
    private String gender;  // 性别
    private String avatarUrl;  // 头像URL
    private String status;  // 账户状态
    private LocalDateTime lastLoginTime;  // 最后登录时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 构造方法
    public User() {
    }

    public User(String realName, String studentNumber, String password, UserRole role) {
        this.realName = realName;
        this.studentNumber = studentNumber;
        this.password = password;
        this.role = role;
    }

    // Getter和Setter方法
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", realName='" + realName + '\'' +
                ", studentNumber='" + studentNumber + '\'' +
                ", role=" + role +
                ", email='" + email + '\'' +
                '}';
    }
}
