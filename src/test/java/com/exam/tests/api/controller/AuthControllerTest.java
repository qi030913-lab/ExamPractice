package com.exam.tests.api.controller;

import com.exam.api.controller.AuthController;
import com.exam.api.dto.AuthLoginRequest;
import com.exam.api.dto.AuthRegisterRequest;
import com.exam.api.security.AuthTokenService;
import com.exam.model.User;
import com.exam.model.enums.UserRole;
import com.exam.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {
    private UserService userService;
    private AuthTokenService authTokenService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        authTokenService = mock(AuthTokenService.class);
        authController = new AuthController(userService, authTokenService);
    }

    @Test
    void loginShouldAcceptLowercaseRole() {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setRole("student");
        request.setRealName("Alice");
        request.setLoginId("2023001");
        request.setPassword("secret123");

        User user = new User("Alice", "2023001", "hashed", UserRole.STUDENT);
        user.setUserId(1);
        when(userService.login("Alice", "2023001", "secret123", UserRole.STUDENT)).thenReturn(user);
        when(authTokenService.issueToken(user)).thenReturn("token-1");

        authController.login(request);

        verify(userService).login(eq("Alice"), eq("2023001"), eq("secret123"), eq(UserRole.STUDENT));
        verify(authTokenService).issueToken(user);
    }

    @Test
    void loginShouldRejectUnsupportedRole() {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setRole("admin");
        request.setRealName("Alice");
        request.setLoginId("2023001");
        request.setPassword("secret123");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authController.login(request));

        assertEquals("角色仅支持 STUDENT 或 TEACHER", exception.getMessage());
    }

    @Test
    void registerShouldRejectBlankRole() {
        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setRole("   ");
        request.setRealName("Alice");
        request.setLoginId("2023001");
        request.setPassword("secret123");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authController.register(request));

        assertEquals("角色不能为空", exception.getMessage());
    }
}
