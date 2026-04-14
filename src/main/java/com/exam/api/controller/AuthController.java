package com.exam.api.controller;

import com.exam.api.common.ApiResponse;
import com.exam.api.dto.AuthLoginRequest;
import com.exam.api.dto.AuthRegisterRequest;
import com.exam.api.dto.AuthUserResponse;
import com.exam.api.security.AuthCookieService;
import com.exam.api.security.AuthenticatedUser;
import com.exam.api.security.AuthTokenService;
import com.exam.exception.AuthenticationException;
import com.exam.model.User;
import com.exam.model.enums.UserRole;
import com.exam.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final AuthCookieService authCookieService;

    public AuthController(UserService userService, AuthTokenService authTokenService, AuthCookieService authCookieService) {
        this.userService = userService;
        this.authTokenService = authTokenService;
        this.authCookieService = authCookieService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthUserResponse> login(
            @Valid @RequestBody AuthLoginRequest request,
            HttpServletResponse response
    ) {
        UserRole role = UserRole.fromCode(request.getRole());
        User user = userService.login(
                request.getRealName(),
                request.getLoginId(),
                request.getPassword(),
                role
        );
        String token = authTokenService.issueToken(user);
        authCookieService.writeLoginCookie(response, token, authTokenService.getTokenTtl());

        return ApiResponse.success("登录成功", AuthUserResponse.from(user));
    }

    @PostMapping("/register")
    public ApiResponse<AuthUserResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        UserRole role = UserRole.fromCode(request.getRole());

        User user = new User();
        user.setRealName(request.getRealName().trim());
        user.setLoginId(request.getLoginId().trim());
        user.setPassword(request.getPassword());
        user.setRole(role);
        user.setGender("MALE");
        user.setStatus("ACTIVE");

        Integer userId = userService.register(user);
        User createdUser = userService.getUserById(userId);

        return ApiResponse.success("注册成功，请登录", AuthUserResponse.from(createdUser));
    }

    @GetMapping("/session")
    public ApiResponse<AuthUserResponse> currentSession(HttpServletRequest request) {
        AuthenticatedUser authenticatedUser = requireAuthenticatedUser(request);
        User currentUser = userService.getUserById(authenticatedUser.getUserId());
        if (currentUser == null) {
            throw new AuthenticationException("登录已失效，请重新登录");
        }
        return ApiResponse.success("会话有效", AuthUserResponse.from(currentUser));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = authCookieService.resolveToken(request);
        if (token != null) {
            authTokenService.invalidateToken(token);
        }
        authCookieService.clearAuthCookie(response);
        return ApiResponse.success("退出登录成功", null);
    }

    private AuthenticatedUser requireAuthenticatedUser(HttpServletRequest request) {
        String token = authCookieService.resolveToken(request);
        AuthenticatedUser authenticatedUser = authTokenService.getAuthenticatedUser(token);
        if (authenticatedUser == null) {
            throw new AuthenticationException("登录已失效，请重新登录");
        }
        return authenticatedUser;
    }
}
