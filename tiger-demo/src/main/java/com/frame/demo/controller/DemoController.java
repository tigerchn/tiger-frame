package com.frame.demo.controller;

import com.frame.demo.model.LoginResponse;
import com.frame.demo.model.RegisterRequest;
import com.frame.demo.model.UserInfoResponse;
import com.frame.mail.message.MailMessage;
import com.frame.mail.sender.MailSender;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/demo")
@Tag(name = "示例模块", description = "示例接口")
@RequiredArgsConstructor
public class DemoController {

    private final MailSender mailSender;

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    @Operation(
            summary = "用户登录",
            description = "用户通过手机号+密码登录，返回JWT Token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "登录成功",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "账号密码错误"),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误")
            }
    )
    @Parameters({
            @Parameter(name = "phone", description = "手机号", required = true, example = "13800138000"),
            @Parameter(name = "password", description = "密码（MD5加密）", required = true, example = "e10adc3949ba59abbe56e057f20f883e")
    })
    public LoginResponse login(@RequestParam String phone, @RequestParam String password) {
        // 业务逻辑...
        return new LoginResponse("token_123456", "admin", phone);
    }

    /**
     * 获取用户信息（需要Token认证）
     */
    @GetMapping("/info/{userId}")
    @Operation(
            summary = "获取用户详情",
            description = "根据用户ID查询用户基本信息，需携带JWT Token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Token无效/过期"),
                    @ApiResponse(responseCode = "404", description = "用户不存在")
            }
    )
    @Parameter(name = "userId", description = "用户ID", required = true, example = "1001")
    public UserInfoResponse getUserInfo(@PathVariable Long userId) {
        // 业务逻辑...
        return new UserInfoResponse(userId, "admin", "13800138000", "admin@xxx.com");
    }

    /**
     * 注册用户（入参校验）
     */
    @PostMapping("/register")
    @Operation(
            summary = "用户注册",
            description = "新用户注册，参数需满足校验规则",
            responses = {
                    @ApiResponse(responseCode = "200", description = "注册成功"),
                    @ApiResponse(responseCode = "400", description = "参数校验失败")
            }
    )
    public String register(@Valid @RequestBody RegisterRequest request) {
        // 业务逻辑...
        return "注册成功";
    }

    @GetMapping("/sendEmail")
    @Operation(
            summary = "发送邮件",
            description = "发送邮件",
            responses = {
                    @ApiResponse(responseCode = "200", description = "邮件发送成功"),
                    @ApiResponse(responseCode = "500", description = "邮件发送失败")
            }
    )
    public String sendEmail() {
        MailMessage mailMessage = MailMessage.builder()
                .receiver("liuxmchn@sina.com")
                .subject("测试邮件")
                .content("这是一封测试邮件")
                .annexFiles(Collections.singletonList(new MailMessage.AnnexFileInfo("test.txt", "hello send mail".getBytes())))
                .build();

        mailSender.sendEmail(mailMessage);

        // 业务逻辑...
        return "邮件发送成功";
    }
}
