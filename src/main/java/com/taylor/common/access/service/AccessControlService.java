package com.taylor.common.access.service;

import com.taylor.common.access.domain.Frozen;

import java.util.List;

public interface AccessControlService {

    /**
     * 判断是否限流
     * @author loveCamille
     * @param key key
     * @param limit 限制次数
     * @return 是否限流
    */
    boolean isRateLimited(String key, int limit);

    /**
     * 修改访问次数
     * @author loveCamille
     * @param key key
     * @param timeout 过期时间
    */
    void recordVisit(String key, int timeout);

    void removeVisit(String key);

    boolean isBanned(String key);

    void incrementFailAttempts(String key, int timeout, int maxFailAttempts, int blockDurationSeconds);

    void removeBanned(String key);

    Frozen getBanned(String key);

    List<Frozen> getBannedList(String keyPattern);

}
