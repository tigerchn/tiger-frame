package com.frame.redis.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson分布式锁配置
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis.redisson")
@Data
public class RedissonConfig {

    // Redis地址（单机：redis://127.0.0.1:6379；集群：rediss://...）
    private String address;
    // 密码（可选）
    private String password;
    // 数据库索引
    private int database = 0;
    // 连接超时时间（毫秒）
    private int connectTimeout = 3000;
    // 超时重试次数
    private int retryAttempts = 3;
    // 重试间隔（毫秒）
    private int retryInterval = 1000;

    /**
     * 创建RedissonClient实例（单机模式，可扩展为集群/哨兵）
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 单机模式配置
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                .setConnectTimeout(connectTimeout)
                .setRetryAttempts(retryAttempts)
                .setRetryInterval(retryInterval);
        // 密码非空时设置
        if (password != null && !password.isEmpty()) {
            singleServerConfig.setPassword(password);
        }
        // 集群模式示例（按需切换）
        // config.useClusterServers()
        //     .addNodeAddress("redis://127.0.0.1:6379", "redis://127.0.0.1:6380")
        //     .setPassword(password);

        return Redisson.create(config);
    }
}

/**
 * Redisson分布式锁配置
 * spring:
 * redis:
 * # 基础Redis配置
 * host: 127.0.0.1
 * port: 6379
 * password: "" # 你的Redis密码
 * database: 0
 * # Redisson配置
 * redisson:
 * address: redis://${spring.redis.host}:${spring.redis.port}
 * password: ${spring.redis.password}
 * database: ${spring.redis.database}
 * connect-timeout: 3000
 * retry-attempts: 3
 * retry-interval: 1000
 * 示例
 * 1. 固定锁名
 *
 * @DistributedLock(key = "stock:save", waitTime = 3)
 * public void saveStock(Long stockId) {
 * // 业务逻辑
 * }
 * <p>
 * 2. 动态锁名（SPEL 表达式，最常用）
 * @DistributedLock(key = "#orderId", prefix = "order:pay:")
 * public void payOrder(String orderId) {
 * // 扣减库存、创建订单、支付逻辑
 * }
 * <p>
 * 3. 从对象中取值
 * @DistributedLock(key = "#user.id", prefix = "user:update:")
 * public void updateUser(User user) {
 * // 更新用户信息
 * }
 * <p>
 * 4. 自定义超时与提示
 * @DistributedLock( key = "#skuId",
 * waitTime = 2,
 * leaseTime = 10,
 * failMsg = "秒杀火爆，请重试！"
 * )
 * public void secKill(String skuId) {
 * // 秒杀逻辑
 * }
 * <p>
 * 注意
 * @EnableAspectJAutoProxy(proxyTargetClass = true)
 * 在Spring Boot启动类或配置类上添加 @EnableAspectJAutoProxy(proxyTargetClass = true)
 * @ComponentScan(basePackages = "com.tiger.common.redis.lock")
 * @ExceptionHandler(ShareLockException.class) public AjaxResult handleShareLockException(ShareLockException e) {
 * log.error("分布式锁异常：{}", e.getMessage());
 * return AjaxResult.error(e.getMessage());
 * }
 *
 *
 */