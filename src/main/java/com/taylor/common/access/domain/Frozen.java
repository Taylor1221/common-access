package com.taylor.common.access.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 冻结信息
 *
 * @author loveCamille
 * @date 2025-04-08 15:41:26
 */
@Getter
@Setter
@Accessors(chain = true)
public class Frozen {

    private int failAttempts = 0;

    private boolean frozen = false;

    private Date startTime;

    private Date expireAfter;

}
