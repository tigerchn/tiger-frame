package com.frame.redis.uitl;


import com.alibaba.fastjson2.JSON;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_SEPARATOR = ".";

    private DefaultRedisScript<Boolean> casScript;

    private DefaultRedisScript<Long> unlockScript;

    @PostConstruct
    public void init() {
        casScript = new DefaultRedisScript<>();
        casScript.setResultType(Boolean.class);
        casScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("compareAndSet.lua")));
        log.info("CAS script initialized: {}", JSON.toJSON(casScript));

        unlockScript = new DefaultRedisScript<>();
        unlockScript.setResultType(Long.class);
        unlockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("unlock.lua")));
        log.info("Unlock script initialized: {}", JSON.toJSON(unlockScript));
    }

    /**
     * 设置缓存值
     *
     * @param key   缓存键
     * @param value 缓存值，支持任意类型
     * @param <T>   值类型
     */
    public <T> void set(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     * @param <T> 返回值类型，由调用方推断
     * @return 缓存值，不存在时返回 null
     */
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 构建多级缓存键，以 "." 连接各部分
     *
     * @param parts 键的各部分
     * @return 拼接后的完整键
     */
    public static String buildKey(String... parts) {
        return String.join(CACHE_KEY_SEPARATOR, parts);
    }

    /**
     * 判断指定键是否存在
     *
     * @param key 缓存键
     * @return true 表示存在，false 表示不存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 删除指定键
     *
     * @param key 缓存键
     * @return true 表示删除成功，false 表示键不存在或删除失败
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 比较并设置（CAS），仅当旧值匹配时才更新为新值
     *
     * @param key      缓存键
     * @param oldValue 期望的旧值
     * @param newValue 新值
     * @return true 表示 CAS 成功，false 表示旧值不匹配
     */
    public boolean compareAndSet(String key, Long oldValue, Long newValue) {
        List<String> keys = Collections.singletonList(key);
        return Boolean.TRUE.equals(redisTemplate.execute(casScript, keys, oldValue, newValue));
    }

    /**
     * 仅在键不存在时设置值，并指定过期时间
     *
     * @param key      缓存键
     * @param value    缓存值
     * @param timeout  过期时间数值
     * @param timeUnit 时间单位
     * @param <T>      值类型
     * @return true 表示设置成功，false 表示键已存在
     */
    public <T> boolean setIfAbsent(String key, T value, long timeout, TimeUnit timeUnit) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit));
    }

    /**
     * 向有序集合中添加成员
     *
     * @param key    缓存键
     * @param member 成员
     * @param score  分数
     * @return true 表示添加成功，false 表示成员已存在且分数未变化
     */
    public boolean zSetAdd(String key, String member, Long score) {
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, member, score.doubleValue()));
    }

    /**
     * 获取有序集合的成员数量
     *
     * @param key 缓存键
     * @return 成员数量
     */
    public Long zSetSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 按排名范围获取有序集合成员（升序）
     *
     * @param key   缓存键
     * @param start 起始位置（包含）
     * @param end   结束位置（包含）
     * @return 成员集合
     */
    public Set<Object> zSetRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 从有序集合中移除指定成员
     *
     * @param key    缓存键
     * @param member 待移除的成员
     * @return 实际移除的成员数量
     */
    public Long zSetRemove(String key, Object member) {
        return redisTemplate.opsForZSet().remove(key, member);
    }

    /**
     * 批量从有序集合中移除成员
     *
     * @param key     缓存键
     * @param members 待移除的成员集合
     * @return 实际移除的成员数量
     */
    public Long zSetRemoveBatch(String key, Set<Object> members) {
        if (members == null || members.isEmpty()) {
            return 0L;
        }
        long removed = 0L;
        for (Object member : members) {
            Long count = redisTemplate.opsForZSet().remove(key, member);
            if (count != null) {
                removed += count;
            }
        }
        return removed;
    }

    /**
     * 获取成员在有序集合中的分数
     *
     * @param key    缓存键
     * @param member 成员
     * @return 分数，成员不存在时返回 null
     */
    public Double zSetScore(String key, Object member) {
        return redisTemplate.opsForZSet().score(key, member);
    }

    /**
     * 按分数范围获取有序集合成员（升序）
     *
     * @param key   缓存键
     * @param start 起始分数（包含）
     * @param end   结束分数（包含）
     * @return 成员集合
     */
    public Set<Object> zSetRangeByScore(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeByScore(key, (double) start, (double) end);
    }

    /**
     * 为有序集合中的成员增加指定分数
     *
     * @param key    缓存键
     * @param member 成员
     * @param score  增加的分数
     * @return 增加后的新分数
     */
    public Double zSetIncrementScore(String key, Object member, double score) {
        return redisTemplate.opsForZSet().incrementScore(key, member, score);
    }

    /**
     * 获取成员在有序集合中的排名（升序，从 0 开始）
     *
     * @param key    缓存键
     * @param member 成员
     * @return 排名，成员不存在时返回 null
     */
    public Long zSetRank(String key, Object member) {
        return redisTemplate.opsForZSet().rank(key, member);
    }

    /**
     * 原子解锁：仅当键值与请求标识匹配时才删除键
     *
     * @param key       锁键
     * @param requestId 请求标识
     * @return true 表示解锁成功，false 表示键不存在或标识不匹配
     */
    public boolean unlock(String key, String requestId) {
        List<String> keys = Collections.singletonList(key);
        Long result = redisTemplate.execute(unlockScript, keys, requestId);
        return result != null && result > 0;
    }

}
