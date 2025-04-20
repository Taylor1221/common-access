package com.taylor.common.access.service;

import com.taylor.common.access.domain.BlockDTO;


public interface AttemptBlockService {

    void recordAttempt(String key);

    int getAttemptCount(String key);

    void block(BlockDTO blockDTO);

    boolean isBlocked(String key);
    
    void reset(String key);

}
