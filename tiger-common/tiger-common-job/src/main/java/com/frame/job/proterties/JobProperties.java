package com.frame.job.proterties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "tiger.job")
@Component
public class JobProperties {

    private String adminAddresses;

    private String accessToken;

    private String appName;

    private String address;

    private String ip;

    private int port;

    private String logPath;

    private int logRetentionDays;
}
