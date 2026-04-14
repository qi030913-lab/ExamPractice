package com.exam.tests.api.security;

import com.exam.api.security.AuthCookieService;
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

import java.time.Clock;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthInterceptorTest {
    private AuthTokenService authTokenService;
    private AuthCookieService authCookieService;
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        authTokenService = mock(AuthTokenService.class);
        authCookieService = new AuthCookieService("EXAM_SESSION", false, "Lax");
        authInterceptor = new AuthInterceptor(authTokenService, authCookieService);
    }

    @Test
    void preHandleShouldPassWhenCookieTokenMatchesUserId() {
        String token = "token-1";
        when(authTokenService.getAuthenticatedUser(token))
                .thenReturn(new com.exam.api.security.AuthenticatedUser(7, UserRole.STUDENT, null));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/student/7/papers");
        request.setCookies(new jakarta.servlet.http.Cookie("EXAM_SESSION", token));
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
    void preHandleShouldPreferBearerTokenWhenBothBearerAndCookieExist() {
        String bearerToken = "bearer-token";
        when(authTokenService.getAuthenticatedUser(bearerToken))
                .thenReturn(new com.exam.api.security.AuthenticatedUser(7, UserRole.STUDENT, null));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/student/7/papers");
        request.addHeader("Authorization", "Bearer " + bearerToken);
        request.setCookies(new jakarta.servlet.http.Cookie("EXAM_SESSION", "cookie-token"));
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("userId", "7"));

        assertDoesNotThrow(() -> authInterceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void preHandleShouldThrowWhenUserIdDoesNotMatchToken() {
        String token = "token-1";
        when(authTokenService.getAuthenticatedUser(token))
                .thenReturn(new com.exam.api.security.AuthenticatedUser(7, UserRole.STUDENT, null));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/student/8/papers");
        request.setCookies(new jakarta.servlet.http.Cookie("EXAM_SESSION", token));
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("userId", "8"));

        assertThrows(AuthenticationException.class,
                () -> authInterceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }
}
