package com.taylor.common.access.service.impl;

import com.taylor.common.access.service.RateLimiter;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Redisson分布式实现
 *
 * @author loveCamille
 * @date 2025-04-19 00:06:45
 */
public class RedissonRateLimiter implements RateLimiter {

    /**
     * 分布式锁前缀
     */
    private static final String LOCK_PREFIX = "common:rate_limit:lock:";

    private final RedissonClient redissonClient;

    public RedissonRateLimiter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean tryAcquire(String key, int limit, long time, TimeUnit unit) {
        // 检查现有实例
        RRateLimiter limiter = getRateLimiter(key, limit, Duration.ofMillis(unit.toMillis(time)));
        return limiter != null && limiter.tryAcquire(1);
    }

    private RRateLimiter getRateLimiter(String key, int limit, Duration rateInterval) {
        // 检查现有实例
        RRateLimiter limiter = redissonClient.getRateLimiter(key);
        if (limiter.isExists()) {
            return limiter;
        }

        // 使用分布式锁初始化
        Lock lock = redissonClient.getLock(LOCK_PREFIX + key);
        try {
            lock.lock();
            // 双重检查
            if (limiter.isExists()) {
                return limiter;
            }
            // 设置限流规则并添加自动过期
            // 每过 rateInterval 补充 limit 个令牌
            if (limiter.trySetRate(RateType.OVERALL, limit, rateInterval)) {
                // 设置Redis键过期时间
                limiter.expire(rateInterval);
                return limiter;
            }
        } finally {
            lock.unlock();
        }
        return null;
    }
}
