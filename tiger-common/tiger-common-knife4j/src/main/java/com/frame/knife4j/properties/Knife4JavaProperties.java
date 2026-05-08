package com.frame.knife4j.properties;

import com.frame.knife4j.model.Contact;
import com.frame.knife4j.model.License;
import com.frame.knife4j.model.Server;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "tiger.knife4j")
@Data
public class Knife4JavaProperties {

    // 基础信息
    private String title;
    private String description;
    private String version;

    // 联系人
    private Contact contact;

    // 许可证
    private License license;

    // 服务器列表
    private List<Server> servers;
}
