package com.frame.knife4j.config;

import com.frame.knife4j.properties.Knife4JavaProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class Knife4jConfig {

    private final Knife4JavaProperties knife4JavaProperties;

    // 注入配置类
    public Knife4jConfig(Knife4JavaProperties knife4JavaProperties) {
        this.knife4JavaProperties = knife4JavaProperties;
    }

    @Bean
    public OpenAPI openAPI() {
        // 服务器配置
        List<Server> servers = knife4JavaProperties.getServers().stream()
                .map(s -> new Server().url(s.getUrl()).description(s.getDescription()))
                .collect(Collectors.toList());

        // ====================== 全局 Token 认证配置 ======================
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER);

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        // ====================== 文档基础信息 ======================
        Info info = new Info()
                .title(knife4JavaProperties.getTitle())
                .description(knife4JavaProperties.getDescription())
                .version(knife4JavaProperties.getVersion())
                .contact(new Contact()
                        .name(knife4JavaProperties.getContact().getName())
                        .email(knife4JavaProperties.getContact().getEmail())
                        .url(knife4JavaProperties.getContact().getUrl()))
                .license(new License()
                        .name(knife4JavaProperties.getLicense().getName())
                        .url(knife4JavaProperties.getLicense().getUrl()));

        return new OpenAPI()
                .info(info)
                .servers(servers)
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}