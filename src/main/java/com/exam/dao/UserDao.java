package com.exam.dao;

import com.exam.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserDao {
    User findStudentByLoginId(@Param("realName") String realName, @Param("loginId") String loginId);

    User findTeacherByLoginId(@Param("realName") String realName, @Param("loginId") String loginId);

    User findById(@Param("userId") Integer userId);

    User findByLoginId(@Param("loginId") String loginId);

    List<User> findAllStudents();

    int insert(User user);

    int update(User user);

    int updatePassword(@Param("userId") Integer userId, @Param("password") String password);

    int delete(@Param("userId") Integer userId);
}
