package com.taylor.common.access.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.taylor.common.access.domain.Frozen;
import com.taylor.common.access.service.AccessControlService;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;
import org.redisson.api.options.KeysScanParams;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


/**
 * 基于Redis实现
 *
 * @author loveCamille
 * @date 2025-04-08 16:11:59
 */
public class RedisAccessControlService implements AccessControlService {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public boolean isRateLimited(String key, int limit) {
        RLock lock = redissonClient.getLock(key);
        lock.lock();
        try {
            RBucket<Integer> bucket = redissonClient.getBucket(key);
            // 不存在key，那肯定之前没有操作过
            if (!bucket.isExists()) return false;
            // 操作次数大于设定的次数
            return bucket.get() >= limit;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void recordVisit(String key, int timeout) {
        RLock lock = redissonClient.getLock(key);
        lock.lock();
        try {
            RBucket<Integer> bucket = redissonClient.getBucket(key);
            if (!bucket.isExists()) {
                bucket.set(1, Duration.ofSeconds(timeout));
            } else {
                Integer count = bucket.get();
                bucket.set(count + 1);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeVisit(String key) {
        RBucket<Integer> bucket = redissonClient.getBucket(key);
        if (bucket.isExists()) {
            bucket.delete();
        }
    }

    @Override
    public boolean isBanned(String key) {
        RLock lock = redissonClient.getLock(key);
        lock.lock();
        try {
            RBucket<Frozen> bucket = redissonClient.getBucket(key);
            // 不存在key，那肯定之前没有操作过
            if (!bucket.isExists()) return false;
            Frozen frozen = bucket.get();
            // 操作次数大于设定的次数
            return frozen.isFrozen();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void incrementFailAttempts(String key, int timeout, int maxFailAttempts, int blockDurationSeconds) {
        RLock lock = redissonClient.getLock(key);
        lock.lock();
        try {
            RBucket<Frozen> bucket = redissonClient.getBucket(key);
            Frozen frozen = bucket.isExists() ? bucket.get() : new Frozen();
            frozen.setFailAttempts(frozen.getFailAttempts() + 1);
            if (frozen.getFailAttempts() < maxFailAttempts) {
                // 没达到最大失败次数，设置信息，redis key的过期时间设置为 timeout
                bucket.set(frozen, Duration.ofSeconds(timeout));
            } else {
                // 达到最大失败次数，设置封禁起始及截至时间，redis key的过期时间设置成 blockDurationSeconds
                DateTime now = DateUtil.date();
                DateTime expireAfter = DateUtil.offsetSecond(now, blockDurationSeconds);
                frozen.setStartTime(now).setExpireAfter(expireAfter).setFrozen(true);
                bucket.set(frozen, Duration.ofSeconds(blockDurationSeconds));
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeBanned(String key) {
        RBucket<Frozen> bucket = redissonClient.getBucket(key);
        if (bucket.isExists()) {
            bucket.delete();
        }
    }

    @Override
    public Frozen getBanned(String key) {
        RBucket<Frozen> bucket = redissonClient.getBucket(key);
        if (!bucket.isExists()) return null;
        return bucket.get();
    }

    @Override
    public List<Frozen> getBannedList(String keyPattern) {
        KeysScanOptions options = new KeysScanParams().pattern(keyPattern);
        Iterable<String> keys = redissonClient.getKeys().getKeys(options);
        List<Frozen> list = new ArrayList<>();
        for (String key : keys) {
            Frozen frozen = getBanned(key);
            if (frozen.isFrozen()) list.add(frozen);
        }
        return list;
    }


}
