package com.taylor.common.access.service;

import java.util.concurrent.TimeUnit;

/**
 * 限流器核心接口
 */
public interface RateLimiter {

    /**
     * 尝试获取访问许可
     * @param key 限流键
     * @param limit 时间窗口内允许的最大请求数
     * @param time 时间窗口数值
     * @param unit 时间单位
     * @return true-允许访问，false-需要限流
     */
    boolean tryAcquire(String key, int limit, long time, TimeUnit unit);

}
