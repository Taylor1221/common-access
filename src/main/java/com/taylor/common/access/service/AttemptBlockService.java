package com.taylor.common.access.service;

import com.taylor.common.access.domain.BlockDTO;

import java.util.List;


public interface AttemptBlockService {

    /**
     * 记录一次失败尝试，可能触发禁止逻辑。
     * @param key 需要记录的标识
     */
    void recordAttempt(String key);

    /**
     * 获取指定键已经尝试的次数
     * @author loveCamille
     * @param key 需要获取次数的唯一标识
     * @return 已经尝试的次数
    */
    int getAttemptCount(String key);

    /**
     * 封禁
     * @author loveCamille
     * @param blockDTO 封禁信息
    */
    void block(BlockDTO blockDTO);

    /**
     * 检查指定键是否已被禁止。
     * @param key 需要检查的唯一标识
     * @return 如果被禁止返回 true，否则返回 false
     */
    boolean isBlocked(String key);

    /**
     * 重置
     * @author loveCamille
     * @param key 需要检查的唯一标识
    */
    void reset(String key);

    List<BlockDTO> getAllBlockRecords();

}
