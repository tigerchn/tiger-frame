package com.frame.mail.sender;

import com.frame.mail.config.MailConfig;
import com.frame.mail.message.MailMessage;
import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;

@Component
@RequiredArgsConstructor
public class MailSender {

    private final MailConfig mailConfig;

    private volatile Properties mailProperties;

    /**
     * 发送邮件
     *
     * @param mailMessage 邮件内容信息
     */
    public void sendEmail(MailMessage mailMessage) {
        // 创建MineMessage,配置各项参数
        final Properties mailProperties = buildEmailProperties();
        final Session session = Session.getInstance(mailProperties);

        // 连接SMTP服务器
        try {
            final MimeMessage message = this.buildEmailMsg(session, mailMessage);
            Transport transport = session.getTransport();
            transport.connect(mailConfig.getSmtpUser(), mailConfig.getSmtpPwd());
            // 调用发送接口
            transport.sendMessage(message, message.getAllRecipients());
            // 邮件发送完毕
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 构建用于邮件发送的配置信息
     *
     * @return Properties
     */
    private Properties buildEmailProperties() {
        if (mailProperties == null) {
            synchronized (this) {
                // 双重检查避免初始化
                if (mailProperties == null) {
                    mailProperties = new Properties();
                    mailProperties.setProperty("mail.smtp.host", mailConfig.getSmtpHost());
                    mailProperties.setProperty("mail.transport.protocol", mailConfig.getSmtpProtocol());
                    mailProperties.setProperty("mail.smtp.auth", "true");
                    mailProperties.setProperty("mail.smtp.port", String.valueOf(mailConfig.getSmtpPort()));
                    mailProperties.setProperty("mail.smtp.connectiontimeout", "100000");
                    mailProperties.setProperty("mail.sender.account", mailConfig.getSmtpUser());
                    mailProperties.setProperty("mail.sender.password", mailConfig.getSmtpPwd());
                    mailProperties.setProperty("mail.sender.nickName", mailConfig.getSenderName());
                    mailProperties.setProperty("mail.smtp.timeout", "25000");
                    // 如果不启用ssl就需要进行配置
                    if (mailConfig.getSmtpSslEnable()) {
                        mailProperties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                        mailProperties.setProperty("mail.smtp.socketFactory.fallback", "false");
                        mailProperties.setProperty("mail.smtp.socketFactory.port", String.valueOf(mailConfig.getSmtpPort()));
                    }
                }
            }
        }
        return mailProperties;
    }

    /**
     * 构建邮件消息内容
     *
     * @param session     Email发送的Session信息
     * @param mailMessage 消息实体
     * @return 邮件消息
     * @throws MessagingException 消息生成异常
     * @throws IOException        读取内容信息异常
     */
    private MimeMessage buildEmailMsg(Session session, MailMessage mailMessage)
            throws MessagingException, IOException {
        final MimeMessage msg = new MimeMessage(session);
        // 发件人
        msg.setFrom(new InternetAddress(mailConfig.getSmtpUser(), mailConfig.getSenderName(), StandardCharsets.UTF_8.name()));
        // 收件人
        msg.addRecipients(Message.RecipientType.TO, mailMessage.getReceiver());
        // 主题
        msg.setSubject(mailMessage.getSubject());
        final MimeMultipart mm = new MimeMultipart();
        // 正文
        final MimeBodyPart txtPart = new MimeBodyPart();
        txtPart.setContent(mailMessage.getContent(), "text/html;charset=UTF-8");
        mm.addBodyPart(txtPart);
        // 附件
        MimeBodyPart attachment;
        DataHandler dh;
        for (MailMessage.AnnexFileInfo annexFile : mailMessage.getAnnexFiles()) {
            attachment = new MimeBodyPart();
            dh = new DataHandler(new ByteArrayDataSource(annexFile.getFileContent(), "application/octet-stream;charset=utf-8"));
            attachment.setDataHandler(dh);
            attachment.setFileName(annexFile.getFileName());
            mm.addBodyPart(attachment);
        }
        mm.setSubType("mixed");
        msg.setContent(mm);
        msg.setSentDate(new Date());
        msg.saveChanges();
        return msg;
    }

}
