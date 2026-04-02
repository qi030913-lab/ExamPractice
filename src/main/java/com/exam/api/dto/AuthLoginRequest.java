package com.exam.api.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthLoginRequest {
    @NotBlank(message = "角色不能为空")
    private String role;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    private String account;

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

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
