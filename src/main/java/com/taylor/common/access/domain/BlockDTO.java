package com.taylor.common.access.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Duration;
import java.util.Date;

/**
 * block信息
 *
 * @author loveCamille
 * @date 2025-04-20 22:32:46
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlockDTO implements Serializable {

    /**
     * 封禁键
     */
    @JsonProperty("key")
    private String key;

    /**
     * 封禁时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date blockTime;

    /**
     * 封禁总时长
     */
    private Duration duration;

    /**
     * 剩余时间
     */
    private Duration remainTime;

    /**
     * 封禁来源（可选）
     */
    private String source;

}
