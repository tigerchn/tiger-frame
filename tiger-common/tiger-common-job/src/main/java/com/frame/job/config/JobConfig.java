package com.frame.job.config;

import com.frame.job.proterties.JobProperties;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class JobConfig {

    private final JobProperties jobProperties;

    public JobConfig(JobProperties jobProperties) {
        this.jobProperties = jobProperties;
    }

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(jobProperties.getAdminAddresses());
        xxlJobSpringExecutor.setAppname(jobProperties.getAppName());
        xxlJobSpringExecutor.setAddress(jobProperties.getAddress());
        xxlJobSpringExecutor.setIp(jobProperties.getIp());
        xxlJobSpringExecutor.setPort(jobProperties.getPort());
        xxlJobSpringExecutor.setAccessToken(jobProperties.getAccessToken());
        xxlJobSpringExecutor.setLogPath(jobProperties.getLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(jobProperties.getLogRetentionDays());
        return xxlJobSpringExecutor;
    }

}
