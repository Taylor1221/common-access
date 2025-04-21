package com.taylor.common.access.service.impl;

import com.taylor.common.access.domain.BlockDTO;
import com.taylor.common.access.service.AttemptBlockService;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redisson实现
 *
 * @author loveCamille
 * @date 2025-04-20 22:15:58
 */
public class RedissonAttemptBlockService implements AttemptBlockService {

    private static final String ATTEMPT_KEY_PREFIX = "common:access:attempt:";

    private static final String BLOCK_KEY_PREFIX = "common:access:block:";

    private final RedissonClient redissonClient;

    public RedissonAttemptBlockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void recordAttempt(String key) {
        redissonClient.getAtomicLong(ATTEMPT_KEY_PREFIX + key).incrementAndGet();
    }

    @Override
    public int getAttemptCount(String key) {
        RAtomicLong attemptCounter = redissonClient.getAtomicLong(ATTEMPT_KEY_PREFIX + key);
        return (int) attemptCounter.get();
    }

    @Override
    public void block(BlockDTO blockDTO) {
        RMapCache<String, BlockDTO> blockMap = redissonClient.getMapCache(BLOCK_KEY_PREFIX);
        String key = BLOCK_KEY_PREFIX + blockDTO.getKey();
        blockMap.fastPut(key, blockDTO, blockDTO.getDuration().toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlocked(String key) {
        return redissonClient.getBucket(BLOCK_KEY_PREFIX + key).isExists();
    }

    @Override
    public void reset(String key) {
        redissonClient.getAtomicLong(ATTEMPT_KEY_PREFIX + key).delete();
        redissonClient.getBucket(BLOCK_KEY_PREFIX + key).delete();
    }

    @Override
    public List<BlockDTO> getAllBlockRecords() {
        return Collections.emptyList();
    }

}
