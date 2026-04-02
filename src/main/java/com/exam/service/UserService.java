package com.exam.service;

import com.exam.dao.UserDao;
import com.exam.exception.AuthenticationException;
import com.exam.exception.BusinessException;
import com.exam.model.User;
import com.exam.model.enums.UserRole;
import com.exam.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDao();
    }

    public User login(String realName, String loginId, String password, UserRole role) {
        if (realName == null || realName.trim().isEmpty()) {
            throw new AuthenticationException("姓名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("密码不能为空");
        }
        if (role == null) {
            throw new AuthenticationException("角色不能为空");
        }
        if (loginId == null || loginId.trim().isEmpty()) {
            if (role == UserRole.TEACHER) {
                throw new AuthenticationException("教工号不能为空");
            }
            throw new AuthenticationException("学号不能为空");
        }

        User user;
        if (role == UserRole.TEACHER) {
            user = userDao.findTeacherByLoginId(realName.trim(), loginId.trim());
        } else {
            user = userDao.findStudentByLoginId(realName.trim(), loginId.trim());
        }

        if (user == null || !PasswordUtil.matches(password, user.getPassword())) {
            if (role == UserRole.TEACHER) {
                throw new AuthenticationException("姓名、教工号或密码错误");
            }
            throw new AuthenticationException("姓名、学号或密码错误");
        }

        if (user.getRole() != role) {
            String roleName = role == UserRole.TEACHER ? "教师" : "学生";
            throw new AuthenticationException("当前用户不是" + roleName + "角色");
        }

        if (PasswordUtil.needsMigration(user.getPassword())) {
            String hashed = PasswordUtil.hashPassword(password);
            userDao.updatePassword(user.getUserId(), hashed);
            user.setPassword(hashed);
        }

        return user;
    }

    public int register(User user) {
        validateUser(user);

        User existingUser = userDao.findByLoginId(user.getLoginId());
        if (existingUser != null) {
            if (user.getRole() == UserRole.TEACHER) {
                throw new BusinessException("教工号已存在");
            }
            throw new BusinessException("学号已存在");
        }

        if (PasswordUtil.needsMigration(user.getPassword())) {
            user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
        }

        return userDao.insert(user);
    }

    public int updateUser(User user) {
        if (user.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }

        if (user.getPassword() != null
                && !user.getPassword().trim().isEmpty()
                && PasswordUtil.needsMigration(user.getPassword())) {
            user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
        }

        return userDao.update(user);
    }

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

    public int deleteUser(Integer userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        User user = userDao.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return userDao.delete(userId);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new BusinessException("用户信息不能为空");
        }
        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }
        if (user.getLoginId() == null || user.getLoginId().trim().isEmpty()) {
            if (user.getRole() == UserRole.TEACHER) {
                throw new BusinessException("教工号不能为空");
            }
            throw new BusinessException("学号不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        if (user.getRole() == null) {
            throw new BusinessException("用户角色不能为空");
        }

        if (user.getLoginId().length() < 3 || user.getLoginId().length() > 20) {
            if (user.getRole() == UserRole.TEACHER) {
                throw new BusinessException("教工号长度应在3到20个字符之间");
            }
            throw new BusinessException("学号长度应在3到20个字符之间");
        }

        if (PasswordUtil.needsMigration(user.getPassword()) && user.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于6个字符");
        }
    }

    public List<User> getStudents() {
        return userDao.findAllStudents();
    }
}
