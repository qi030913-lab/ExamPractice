package com.exam.api.dto;

import com.exam.model.User;

public class AuthUserResponse {
    private Integer userId;
    private String realName;
    private String loginId;
    private String role;
    private String token;
    private String email;
    private String phone;
    private String gender;
    private String status;

    public static AuthUserResponse from(User user) {
        return from(user, null);
    }

    public static AuthUserResponse from(User user, String token) {
        AuthUserResponse response = new AuthUserResponse();
        response.userId = user.getUserId();
        response.realName = user.getRealName();
        response.loginId = user.getLoginId();
        response.role = user.getRole() == null ? null : user.getRole().name();
        response.token = token;
        response.email = user.getEmail();
        response.phone = user.getPhone();
        response.gender = user.getGender();
        response.status = user.getStatus();
        return response;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getRealName() {
        return realName;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }

    public String getStatus() {
        return status;
    }
}
