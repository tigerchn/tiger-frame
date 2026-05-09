package com.frame.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户信息响应")
public class UserInfoResponse {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "admin@xxx.com")
    private String email;

    // 构造器、getter/setter...
    public UserInfoResponse(Long userId, String username, String phone, String email) {
        this.userId = userId;
        this.username = username;
        this.phone = phone;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}