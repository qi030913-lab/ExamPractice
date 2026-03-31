package com.exam.tests.service;

import com.exam.dao.UserDao;
import com.exam.exception.AuthenticationException;
import com.exam.model.User;
import com.exam.model.enums.UserRole;
import com.exam.service.UserService;
import com.exam.tests.support.FieldInjector;
import com.exam.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private UserService userService;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        userDao = mock(UserDao.class);
        FieldInjector.setField(userService, "userDao", userDao);
    }

    @Test
    void loginTeacherWithHashedPasswordShouldSucceed() {
        User user = new User("Teacher", "teacher001", PasswordUtil.hashPassword("123456"), UserRole.TEACHER);
        user.setUserId(1);
        when(userDao.findByNameAndPassword("Teacher", "123456")).thenReturn(user);

        User result = userService.login("Teacher", null, "123456", UserRole.TEACHER);

        assertNotNull(result);
        assertEquals(UserRole.TEACHER, result.getRole());
        verify(userDao, never()).updatePassword(any(), any());
    }

    @Test
    void loginStudentWrongPasswordShouldThrow() {
        User user = new User("Student", "2021001", PasswordUtil.hashPassword("right-pass"), UserRole.STUDENT);
        when(userDao.findByNameNumberAndPassword("Student", "2021001", "wrong-pass")).thenReturn(user);

        assertThrows(AuthenticationException.class,
                () -> userService.login("Student", "2021001", "wrong-pass", UserRole.STUDENT));
    }

    @Test
    void loginShouldMigratePlainPasswordOnSuccess() {
        User user = new User("Student", "2021002", "123456", UserRole.STUDENT);
        user.setUserId(12);
        when(userDao.findByNameNumberAndPassword("Student", "2021002", "123456")).thenReturn(user);

        User result = userService.login("Student", "2021002", "123456", UserRole.STUDENT);

        assertNotNull(result);
        assertTrue(result.getPassword().startsWith("PBKDF2$"));
        verify(userDao).updatePassword(eq(12), any(String.class));
    }

    @Test
    void registerShouldHashPasswordBeforeInsert() {
        User user = new User("Alice", "2023001", "abcdef", UserRole.STUDENT);
        when(userDao.findByStudentNumber("2023001")).thenReturn(null);
        when(userDao.insert(any(User.class))).thenReturn(101);

        int id = userService.register(user);

        assertEquals(101, id);
        assertTrue(user.getPassword().startsWith("PBKDF2$"));
        verify(userDao).insert(user);
    }
}
