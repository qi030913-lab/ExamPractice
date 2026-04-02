package com.exam.api.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthRegisterRequest {
    @NotBlank(message = "角色不能为空")
    private String role;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotBlank(message = "登录号不能为空")
    private String loginId;

    @NotBlank(message = "密码不能为空")
    private String password;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
