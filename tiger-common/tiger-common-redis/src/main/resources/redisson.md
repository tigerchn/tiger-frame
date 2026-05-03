# 					全套功能使用示例（一行注解）

#### 0.整合配置

```yaml
# Redisson分布式锁配置
spring:
  redis:
    # 基础Redis配置
    host: 127.0.0.1
    port: 6379
    password: "" # 你的Redis密码
    database: 0
    # Redisson配置
    redisson:
      address: redis://${spring.redis.host}:${spring.redis.port}
      password: ${spring.redis.password}
      database: ${spring.redis.database}
      connect-timeout: 3000
      retry-attempts: 3
      retry-interval: 1000
```



#### 1.基础分布式锁（通用场景）

```java
// 适用：下单、支付、扣库存、修改数据
@DistributedLock(key = "#orderId", prefix = "order:pay:")
public void pay(String orderId) {}
```

#### 2. 分布式锁 + 防重复提交（提交类接口）

```java
// 适用：表单提交、确认订单、充值
@DistributedLock(
    key = "#orderId",
    prefix = "order:submit:",
    repeatSubmit = true,
    repeatInterval = 3
)
public void submitOrder(String orderId) {
    // 业务逻辑
}
```

#### 3. 长任务 + 自动续约

```java
// 适用：导入、导出、批量处理
@DistributedLock(
    key = "#taskId",
    autoRenew = true,
    failMsg = "任务执行中，请勿重复操作"
)
public void executeLongTask(String taskId) {
    // 长耗时业务
}
```

#### 4. 秒杀场景（公平锁 + 防重）

```java
@DistributedLock(
    key = "#skuId",
    lockType = LockType.FAIR,
    repeatSubmit = true,
    failMsg = "秒杀火爆，请稍后重试"
)
public void secKill(String skuId) {
    // 秒杀逻辑
}
```

#### 5. 金融级高可用（RedLock）

```java
@DistributedLock(
    key = "#tradeNo",
    redLock = true,
    failMsg = "交易处理中，请耐心等待"
)
public void payTrade(String tradeNo) {
    // 交易/支付核心逻辑
}
```

#### 6. 读写锁使用（高并发优化）

```java
// 读锁（多人同时读，不加锁）
@DistributedLock(key = "#userId", lockType = LockType.READ)
public User getUserInfo(Long userId) {
    return userMapper.selectById(userId);
}

// 写锁（修改时加锁）
@DistributedLock(key = "#user.id", lockType = LockType.WRITE)
public void updateUser(User user) {
    userMapper.updateById(user);
}
```

#### 7. SPEL 表达式用法

```tex
#id
#order.id
#user.userId
#dto.goodsId
```

```java
// 示例
@DistributedLock(key = "#user.id", prefix = "user:update:")
public void update(User user) { }
```

#### 8.注意

```java
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = "com.tiger.common.redis")

@ExceptionHandler(ShareLockException.class)
public AjaxResult handleShareLockException(ShareLockException e) {
    log.error("分布式锁异常：{}", e.getMessage());
    return AjaxResult.error(e.getMessage());
}
```



