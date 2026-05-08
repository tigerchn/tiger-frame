package com.frame.mail.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 邮件发送的配置信息,其中信息需要自行去邮箱厂商申请
 */
@Component
@Data
public class MailConfig {

    /**
     * 邮件服务的地址
     */
    @Value("${tiger.smtpHost}")
    private String smtpHost;

    /**
     * 邮件服务的协议
     */
    @Value("${tiger.smtpProtocol:smtp}")
    private String smtpProtocol = "smtp";

    /**
     * 邮件服务的端口
     */
    @Value("${tiger.smtpPort}")
    private Integer smtpPort;

    /**
     * 邮件发送人的账号
     */
    @Value("${tiger.smtpUser}")
    private String smtpUser;

    /**
     * 邮件发送人的密码
     */
    @Value("${tiger.smtpPwd}")
    private String smtpPwd;

    /**
     * 是否启用ssl传输
     */
    @Value("${tiger.smtpSslEnable:false}")
    private Boolean smtpSslEnable = true;

    /**
     * 邮件发送人的名字
     */
    @Value("${tiger.senderName}")
    private String senderName;

}
