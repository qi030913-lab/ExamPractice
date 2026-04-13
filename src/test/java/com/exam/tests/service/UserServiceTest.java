package com.exam.tests.service;

import com.exam.dao.UserDao;
import com.exam.exception.AuthenticationException;
import com.exam.model.User;
import com.exam.model.enums.UserRole;
import com.exam.service.UserService;
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
        userDao = mock(UserDao.class);
        userService = new UserService(userDao);
    }

    @Test
    void loginTeacherWithHashedPasswordShouldSucceed() {
        User user = new User("Teacher", "teacher001", PasswordUtil.hashPassword("123456"), UserRole.TEACHER);
        user.setUserId(1);
        when(userDao.findTeacherByLoginId("Teacher", "teacher001")).thenReturn(user);

        User result = userService.login("Teacher", "teacher001", "123456", UserRole.TEACHER);

        assertNotNull(result);
        assertEquals(UserRole.TEACHER, result.getRole());
        verify(userDao, never()).updatePassword(any(), any());
    }

    @Test
    void loginStudentWrongPasswordShouldThrow() {
        User user = new User("Student", "2021001", PasswordUtil.hashPassword("right-pass"), UserRole.STUDENT);
        when(userDao.findStudentByLoginId("Student", "2021001")).thenReturn(user);

        assertThrows(AuthenticationException.class,
                () -> userService.login("Student", "2021001", "wrong-pass", UserRole.STUDENT));
    }

    @Test
    void loginShouldMigratePlainPasswordOnSuccess() {
        User user = new User("Student", "2021002", "123456", UserRole.STUDENT);
        user.setUserId(12);
        when(userDao.findStudentByLoginId("Student", "2021002")).thenReturn(user);

        User result = userService.login("Student", "2021002", "123456", UserRole.STUDENT);

        assertNotNull(result);
        assertTrue(result.getPassword().startsWith("PBKDF2$"));
        verify(userDao).updatePassword(eq(12), any(String.class));
    }

    @Test
    void registerShouldHashPasswordBeforeInsert() {
        User user = new User("Alice", "2023001", "abcdef", UserRole.STUDENT);
        when(userDao.findByLoginId("2023001")).thenReturn(null);
        when(userDao.insert(any(User.class))).thenReturn(101);

        int id = userService.register(user);

        assertEquals(101, id);
        assertTrue(user.getPassword().startsWith("PBKDF2$"));
        verify(userDao).insert(user);
    }

    @Test
    void updateUserShouldKeepExistingPasswordWhenPasswordIsBlank() {
        User existingUser = new User("Alice", "2023001", PasswordUtil.hashPassword("old-pass"), UserRole.STUDENT);
        existingUser.setUserId(101);

        User updateUser = new User();
        updateUser.setUserId(101);
        updateUser.setRealName("Alice Updated");
        updateUser.setEmail("alice@example.com");
        updateUser.setPhone("13800000000");
        updateUser.setPassword("   ");

        when(userDao.findById(101)).thenReturn(existingUser);
        when(userDao.update(updateUser)).thenReturn(1);

        int updated = userService.updateUser(updateUser);

        assertEquals(1, updated);
        assertEquals(existingUser.getPassword(), updateUser.getPassword());
        verify(userDao).update(updateUser);
    }

    @Test
    void updateUserShouldHashNewPasswordWhenProvided() {
        User existingUser = new User("Alice", "2023001", PasswordUtil.hashPassword("old-pass"), UserRole.STUDENT);
        existingUser.setUserId(102);

        User updateUser = new User();
        updateUser.setUserId(102);
        updateUser.setRealName("Alice Updated");
        updateUser.setPassword("new-pass");

        when(userDao.findById(102)).thenReturn(existingUser);
        when(userDao.update(updateUser)).thenReturn(1);

        int updated = userService.updateUser(updateUser);

        assertEquals(1, updated);
        assertTrue(updateUser.getPassword().startsWith("PBKDF2$"));
        verify(userDao).update(updateUser);
    }
}
