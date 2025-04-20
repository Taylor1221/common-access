package com.taylor.common.access.service.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.taylor.common.access.service.RateLimiter;
import com.taylor.common.base.lock.ILock;
import com.taylor.common.base.lock.LocalLock;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 内存方式实现
 *
 * @author loveCamille
 * @date 2025-04-20 14:18:39
 */
public class InMemoryRateLimiter implements RateLimiter {

    private final TimedCache<String, WindowState> cache =
            CacheUtil.newTimedCache(Duration.ofMinutes(1).toMillis());

    private final ILock lock = new LocalLock();

    @Override
    public boolean tryAcquire(String key, int limit, long time, TimeUnit unit) {
        lock.lock(key);
        try {
            // 尝试获取该key的窗口信息
            WindowState currKeyState = cache.get(key);
            long currTime = System.currentTimeMillis();
            // 一个周期的时间
            long rateInterval = unit.toMillis(limit);
            // 该key第一次访问
            if (currKeyState == null) {
                currKeyState = new WindowState(currTime, rateInterval);
                cache.put(key, currKeyState);
            }
            // 当前key上一周期已过期
            if (currTime > currKeyState.windowStart + currKeyState.windowMillis) {
                // 重设状态
                currKeyState.reset(currTime, rateInterval);
            }
            // 当前key的已访问次数 < 所规定次数
            if (currKeyState.count < limit) {
                currKeyState.increment();
                return true;
            }
        } finally {
            lock.unlock(key);
        }
        return false;
    }

    /**
     * 窗口状态内部类
     *
     * @author loveCamille
     * @date 2025-04-20 14:20:03
     */
    private static class WindowState {

        /**
         * 窗口开始时间
         */
        private long windowStart;

        /**
         * 窗口时长
         */
        private long windowMillis;

        /**
         * 当前计数
         */
        private int count = 0;

        private WindowState(long windowStart, long windowMillis) {
            this.windowStart = windowStart;
            this.windowMillis = windowMillis;
            this.count = 0;
        }

        /**
         * 增加访问次数
         * @author loveCamille
         */
        private void increment() {
            count++;
        }

        /**
         * 重设窗口开始时间
         * @author loveCamille
         * @param newStart 新的窗口时间
         */
        private void reset(long newStart, long newMillis) {
            this.windowStart = newStart;
            this.windowMillis = newMillis;
            this.count = 0;
        }

    }

}
