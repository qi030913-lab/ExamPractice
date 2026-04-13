package com.exam.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthLoginRequest {
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "(?i)^(student|teacher)$", message = "角色仅支持 STUDENT 或 TEACHER")
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
