package com.exam.tests.api.security;

import com.exam.api.security.AuthInterceptor;
import com.exam.api.security.AuthTokenService;
import com.exam.exception.AuthenticationException;
import com.exam.model.User;
import com.exam.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthInterceptorTest {
    private AuthTokenService authTokenService;
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        authTokenService = new AuthTokenService();
        authInterceptor = new AuthInterceptor(authTokenService);
    }

    @Test
    void preHandleShouldPassWhenTokenMatchesUserId() {
        String token = issueToken(7, UserRole.STUDENT);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/student/7/papers");
        request.addHeader("Authorization", "Bearer " + token);
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("userId", "7"));

        assertDoesNotThrow(() -> authInterceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        Object attribute = request.getAttribute(AuthInterceptor.AUTHENTICATED_USER_ATTRIBUTE);
        assertEquals(7, ((com.exam.api.security.AuthenticatedUser) attribute).getUserId());
    }

    @Test
    void preHandleShouldThrowWhenAuthorizationMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/student/7/papers");
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("userId", "7"));

        assertThrows(AuthenticationException.class,
                () -> authInterceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void preHandleShouldThrowWhenUserIdDoesNotMatchToken() {
        String token = issueToken(7, UserRole.STUDENT);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/student/8/papers");
        request.addHeader("Authorization", "Bearer " + token);
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("userId", "8"));

        assertThrows(AuthenticationException.class,
                () -> authInterceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    private String issueToken(int userId, UserRole role) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(role);
        return authTokenService.issueToken(user);
    }
}
