package com.exam.api.controller;

import com.exam.api.common.ApiResponse;
import com.exam.api.dto.AuthLoginRequest;
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
                request.getAccount(),
                request.getPassword(),
                role
        );

        return ApiResponse.success("Login succeeded.", AuthUserResponse.from(user));
    }
}
