package com.frame.tool.storage.oss.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.frame.tool.storage.oss.properties.OssProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "aliyun.oss", name = "enabled", havingValue = "true")
public class OssConfig {

    private final OssProperties ossProperties;

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient() {
        return new OSSClientBuilder()
                .build(
                        ossProperties.getEndpoint(),
                        ossProperties.getAccessKeyId(),
                        ossProperties.getAccessKeySecret()
                );
    }
}