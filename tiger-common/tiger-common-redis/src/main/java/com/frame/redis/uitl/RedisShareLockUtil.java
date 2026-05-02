package com.frame.redis.uitl;

import com.frame.redis.exception.ShareLockException;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 企业生产级 Redis 分布式锁
 * 支持：可重入 + 自动续期 + 原子解锁 + 防死锁
 */
@Component
public class RedisShareLockUtil {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // ==================== 常量定义 ====================
    private static final String LOCK_PREFIX = "redis:lock:";
    private static final long DEFAULT_EXPIRE = 30000L;
    private static final long SPIN_SLEEP_TIME = 50L;
    private static final long RENEWAL_INTERVAL = 10000L;

    // ==================== 重入 + 续期 ====================
    private final Map<String, AtomicInteger> reentrantCountMap = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService renewalExecutor = new ScheduledThreadPoolExecutor(1);

    // ==================== Lua 脚本 ====================
    private static final String LOCK_LUA =
            "local key = KEYS[1] " +
                    "local value = ARGV[1] " +
                    "local expire = ARGV[2] " +
                    "if redis.call('exists', key) == 0 then " +
                    "   redis.call('set', key, value, 'PX', expire) " +
                    "   return 1 " +
                    "end " +
                    "if redis.call('get', key) == value then " +
                    "   return 1 " +
                    "end " +
                    "return 0";

    private static final String UNLOCK_LUA =
            "local key = KEYS[1] " +
                    "local value = ARGV[1] " +
                    "if redis.call('get', key) == value then " +
                    "   return redis.call('del', key) " +
                    "else " +
                    "   return 0 " +
                    "end";

    private static final String RENEWAL_LUA =
            "local key = KEYS[1] " +
                    "local value = ARGV[1] " +
                    "if redis.call('get', key) == value then " +
                    "   redis.call('pexpire', key, ARGV[2]) " +
                    "   return 1 " +
                    "end " +
                    "return 0";

    // ==================== 对外方法 ====================

    /**
     * 加锁（支持重入 + 自动续期）
     */
    public boolean lock(String key, String requestId, long expireTime) {
        if (StringUtils.isAnyBlank(key, requestId) || expireTime <= 0) {
            throw new ShareLockException("分布式锁参数异常");
        }
        String lockKey = LOCK_PREFIX + key;
        expireTime = expireTime == 0 ? DEFAULT_EXPIRE : expireTime;

        // 重入计数
        AtomicInteger count = reentrantCountMap.computeIfAbsent(lockKey, k -> new AtomicInteger(0));
        if (count.get() > 0) {
            count.incrementAndGet();
            return true;
        }

        long outTime = System.currentTimeMillis() + DEFAULT_EXPIRE;
        while (System.currentTimeMillis() < outTime) {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(LOCK_LUA, Long.class);
            Long result = stringRedisTemplate.execute(script, Collections.singletonList(lockKey), requestId, String.valueOf(expireTime));
            if (result != null && result == 1) {
                count.incrementAndGet();
                startRenewal(lockKey, requestId, expireTime);
                return true;
            }

            try {
                Thread.sleep(SPIN_SLEEP_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * 解锁（原子操作）
     */
    public boolean unlock(String key, String requestId) {
        if (StringUtils.isAnyBlank(key, requestId)) {
            throw new ShareLockException("解锁参数异常");
        }
        String lockKey = LOCK_PREFIX + key;

        AtomicInteger count = reentrantCountMap.get(lockKey);
        if (count == null) return false;
        if (count.decrementAndGet() > 0) return true;

        reentrantCountMap.remove(lockKey);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_LUA, Long.class);
        Long result = stringRedisTemplate.execute(script, Collections.singletonList(lockKey), requestId);
        return result != null && result == 1;
    }

    /**
     * 尝试一次加锁
     */
    public boolean tryLock(String key, String requestId, long expireTime) {
        if (StringUtils.isAnyBlank(key, requestId) || expireTime <= 0) {
            throw new ShareLockException("分布式锁参数异常");
        }
        String lockKey = LOCK_PREFIX + key;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LOCK_LUA, Long.class);
        Long result = stringRedisTemplate.execute(script, Collections.singletonList(lockKey), requestId, String.valueOf(expireTime));
        return result != null && result == 1;
    }

    // ==================== 自动续期 ====================
    private void startRenewal(String lockKey, String requestId, long expireTime) {
        renewalExecutor.scheduleAtFixedRate(() -> {
            if (!reentrantCountMap.containsKey(lockKey)) {
                return;
            }
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(RENEWAL_LUA, Long.class);
            stringRedisTemplate.execute(script, Collections.singletonList(lockKey), requestId, String.valueOf(expireTime));
        }, RENEWAL_INTERVAL, RENEWAL_INTERVAL, TimeUnit.MILLISECONDS);
    }
}