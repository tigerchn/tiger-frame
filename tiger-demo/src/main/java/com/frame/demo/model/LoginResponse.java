package com.frame.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class LoginResponse {
    @Schema(description = "JWT Token", example = "token_123456")
    private String token;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    // 构造器、getter/setter
    public LoginResponse(String token, String username, String phone) {
        this.token = token;
        this.username = username;
        this.phone = phone;
    }

    // getter/setter...

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}