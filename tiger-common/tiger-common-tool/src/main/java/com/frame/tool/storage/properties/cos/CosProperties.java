package com.frame.tool.storage.properties.cos;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tencent.cos")
public class CosProperties {

    private String secretId;

    private String secretKey;

    private String bucketName;

    private String region;

}
