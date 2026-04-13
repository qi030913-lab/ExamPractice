package com.exam.api.security;

import com.exam.exception.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

    private final AuthTokenService authTokenService;

    public AuthInterceptor(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = extractBearerToken(request.getHeader("Authorization"));
        if (token == null) {
            throw new AuthenticationException("未登录或登录已失效");
        }

        AuthenticatedUser authenticatedUser = authTokenService.getAuthenticatedUser(token);
        if (authenticatedUser == null) {
            throw new AuthenticationException("登录已失效，请重新登录");
        }

        String requestUserId = getUriVariables(request).get("userId");
        if (requestUserId != null && !requestUserId.equals(String.valueOf(authenticatedUser.getUserId()))) {
            throw new AuthenticationException("当前登录身份与请求用户不匹配");
        }

        request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, authenticatedUser);
        return true;
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null) {
            return null;
        }

        String prefix = "Bearer ";
        if (!authorization.startsWith(prefix)) {
            return null;
        }

        String token = authorization.substring(prefix.length()).trim();
        return token.isEmpty() ? null : token;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getUriVariables(HttpServletRequest request) {
        Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (attribute instanceof Map<?, ?> map) {
            return (Map<String, String>) map;
        }
        return Map.of();
    }
}
