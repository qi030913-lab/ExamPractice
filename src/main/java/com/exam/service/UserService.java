package com.exam.service;

import com.exam.dao.UserDao;
import com.exam.exception.AuthenticationException;
import com.exam.exception.BusinessException;
import com.exam.model.User;
import com.exam.model.enums.UserRole;

/**
 * 用户服务类
 * 处理用户相关的业务逻辑
 */
public class UserService {
    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDao();
    }

    /**
     * 用户登录
     * @param realName 真实姓名
     * @param studentNumber 学号
     * @param password 密码
     * @return 用户对象
     * @throws AuthenticationException 认证失败
     */
    public User login(String realName, String studentNumber, String password) {
        if (realName == null || realName.trim().isEmpty()) {
            throw new AuthenticationException("姓名不能为空");
        }
        if (studentNumber == null || studentNumber.trim().isEmpty()) {
            throw new AuthenticationException("学号不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("密码不能为空");
        }

        User user = userDao.findByNameNumberAndPassword(realName.trim(), studentNumber.trim(), password);
        if (user == null) {
            throw new AuthenticationException("姓名、学号或密码错误");
        }

        return user;
    }

    /**
     * 注册新用户
     * @param user 用户对象
     * @return 用户ID
     * @throws BusinessException 业务异常
     */
    public int register(User user) {
        // 验证用户信息
        validateUser(user);

        // 检查学号是否已存在
        User existingUser = userDao.findByStudentNumber(user.getStudentNumber());
        if (existingUser != null) {
            throw new BusinessException("学号已存在");
        }

        // 添加用户
        return userDao.insert(user);
    }

    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 更新结果
     */
    public int updateUser(User user) {
        if (user.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        return userDao.update(user);
    }

    /**
     * 根据ID查询用户
     * @param userId 用户ID
     * @return 用户对象
     */
    public User getUserById(Integer userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return user;
    }

    /**
     * 验证用户信息
     * @param user 用户对象
     */
    private void validateUser(User user) {
        if (user == null) {
            throw new BusinessException("用户信息不能为空");
        }
        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }
        if (user.getStudentNumber() == null || user.getStudentNumber().trim().isEmpty()) {
            throw new BusinessException("学号不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        if (user.getRole() == null) {
            throw new BusinessException("用户角色不能为空");
        }

        // 验证学号格式（可选）
        if (user.getStudentNumber().length() < 3 || user.getStudentNumber().length() > 20) {
            throw new BusinessException("学号长度应在3-20个字符之间");
        }

        // 验证密码长度
        if (user.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于6个字符");
        }
    }
}
