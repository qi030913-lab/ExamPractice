package com.exam.api.controller;

import com.exam.api.common.ApiResponse;
import com.exam.api.dto.AuthLoginRequest;
import com.exam.api.dto.AuthRegisterRequest;
import com.exam.api.dto.AuthUserResponse;
import com.exam.api.security.AuthTokenService;
import com.exam.model.User;
import com.exam.model.enums.UserRole;
import com.exam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthTokenService authTokenService;

    public AuthController(UserService userService, AuthTokenService authTokenService) {
        this.userService = userService;
        this.authTokenService = authTokenService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthUserResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        UserRole role = UserRole.fromCode(request.getRole());
        User user = userService.login(
                request.getRealName(),
                request.getLoginId(),
                request.getPassword(),
                role
        );
        String token = authTokenService.issueToken(user);

        return ApiResponse.success("登录成功", AuthUserResponse.from(user, token));
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
}
