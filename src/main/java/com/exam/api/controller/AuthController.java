package com.exam.api.controller;

import com.exam.api.common.ApiResponse;
import com.exam.api.dto.AuthLoginRequest;
import com.exam.api.dto.AuthRegisterRequest;
import com.exam.api.dto.AuthUserResponse;
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

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthUserResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        UserRole role = UserRole.valueOf(request.getRole().trim().toUpperCase());
        User user = userService.login(
                request.getRealName(),
                request.getLoginId(),
                request.getPassword(),
                role
        );

        return ApiResponse.success("Login succeeded.", AuthUserResponse.from(user));
    }

    @PostMapping("/register")
    public ApiResponse<AuthUserResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        UserRole role = UserRole.valueOf(request.getRole().trim().toUpperCase());

        User user = new User();
        user.setRealName(request.getRealName().trim());
        user.setLoginId(request.getLoginId().trim());
        user.setPassword(request.getPassword());
        user.setRole(role);
        user.setGender("MALE");
        user.setStatus("ACTIVE");

        Integer userId = userService.register(user);
        User createdUser = userService.getUserById(userId);

        return ApiResponse.success("注册成功，请登录。", AuthUserResponse.from(createdUser));
    }
}
