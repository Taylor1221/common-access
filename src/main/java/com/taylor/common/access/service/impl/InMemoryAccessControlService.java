package com.taylor.common.access.service.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.taylor.common.access.domain.Frozen;
import com.taylor.common.access.service.AccessControlService;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 基于本地内存实现
 *
 * @author loveCamille
 * @date 2025-04-09 11:15:09
 */
public class InMemoryAccessControlService implements AccessControlService {

    private final TimedCache<String, AccessObject<Integer>> rateLimitedCache = CacheUtil.newTimedCache(Duration.ofMinutes(1).toMillis());

    private final TimedCache<String, AccessObject<Frozen>> bannedCache = CacheUtil.newTimedCache(Duration.ofMinutes(1).toMillis());

    @Override
    public boolean isRateLimited(String key, int limit) {
        if (rateLimitedCache.containsKey(key)) return false;
        return rateLimitedCache.get(key, false).data >= limit;
    }

    @Override
    public void recordVisit(String key, int timeout) {
        AccessObject<Integer> accessObject = rateLimitedCache.get(key, false);
        if (accessObject == null) {
            long duration = Duration.ofSeconds(timeout).toMillis();
            rateLimitedCache.put(key, new AccessObject<>(System.currentTimeMillis() + duration, 1), duration);
        } else {
            long duration = accessObject.expireTime - System.currentTimeMillis();
            accessObject.data++;
            rateLimitedCache.put(key, accessObject, duration);
        }
    }

    @Override
    public void removeVisit(String key) {
        if (rateLimitedCache.containsKey(key)) rateLimitedCache.remove(key);
    }

    @Override
    public boolean isBanned(String key) {
        if (!bannedCache.containsKey(key)) return false;
        return bannedCache.get(key, false).data.isFrozen();
    }

    @Override
    public void incrementFailAttempts(String key, int timeout, int maxFailAttempts, int blockDurationSeconds) {
        AccessObject<Frozen> accessObject = bannedCache.get(key, false);
        if (accessObject == null) {
            long duration = Duration.ofSeconds(timeout).toMillis();
            accessObject = new AccessObject<>(System.currentTimeMillis() + duration, new Frozen());
            bannedCache.put(key, accessObject, duration);
        }
        Frozen frozen = accessObject.data;
        frozen.setFailAttempts(frozen.getFailAttempts() + 1);
        if (frozen.getFailAttempts() < maxFailAttempts) {
            long duration = accessObject.expireTime - System.currentTimeMillis();
            bannedCache.put(key, accessObject, duration);
        } else {
            accessObject.expireTime = Duration.ofSeconds(blockDurationSeconds).toMillis();
            frozen.setFrozen(true);
            // 达到最大失败次数，设置封禁起始及截至时间，redis key的过期时间设置成 blockDurationSeconds
            DateTime now = DateUtil.date();
            DateTime expireAfter = DateUtil.offsetSecond(now, blockDurationSeconds);
            frozen.setStartTime(now).setExpireAfter(expireAfter).setFrozen(true);
            bannedCache.put(key, accessObject, accessObject.expireTime);
        }
    }

    @Override
    public void removeBanned(String key) {
        bannedCache.remove(key);
    }

    @Override
    public Frozen getBanned(String key) {
        AccessObject<Frozen> frozenAccessObject = bannedCache.get(key, false);
        return frozenAccessObject == null ? null : frozenAccessObject.data;
    }

    @Override
    public List<Frozen> getBannedList(String keyPattern) {
        if (bannedCache.isEmpty()) return Collections.emptyList();
        List<Frozen> list = new ArrayList<>();
        String regex = keyPattern
                .replace(".", "\\.")   // 转义点号
                .replace("*", ".*")    // 替换星号
                .replace("?", ".");    // 替换问号
        Pattern pattern = Pattern.compile(regex);
        for (String key : bannedCache.keySet()) {
            if (pattern.matcher(key).matches()) {
                Frozen frozen = bannedCache.get(key, false).data;
                list.add(frozen);
            }
        }
        return list;
    }

    @AllArgsConstructor
    @Accessors(chain = true)
    private final static class AccessObject<T> {

        private long expireTime;

        private T data;

    }

}
