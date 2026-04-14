package com.exam.dao;

import com.exam.model.AuthSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface AuthSessionDao {
    void createTableIfMissing();

    int insert(AuthSession session);

    AuthSession findByTokenHash(@Param("tokenHash") String tokenHash);

    int deleteByTokenHash(@Param("tokenHash") String tokenHash);

    int deleteExpiredSessions(@Param("currentTime") LocalDateTime currentTime);

    int countSessions();

    String findOldestTokenHash();
}
