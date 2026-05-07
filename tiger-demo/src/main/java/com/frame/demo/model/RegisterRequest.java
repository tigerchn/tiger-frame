package com.frame.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "注册请求")
public class RegisterRequest {
    @Schema(description = "手机号", example = "13800138000", required = true)
    @jakarta.validation.constraints.Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @Schema(description = "密码", example = "123456aA@", required = true)
    @jakarta.validation.constraints.NotBlank(message = "密码不能为空")
    @jakarta.validation.constraints.Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$", message = "密码需包含大小写字母、数字、特殊字符，长度8-16位")
    private String password;

    // getter/setter...


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}