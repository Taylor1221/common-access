package com.taylor.common.access.exception;

/**
 * 失败异常
 *
 * @author loveCamille
 * @date 2025-04-08 16:43:46
 */
public class FailedException extends RuntimeException {

    public FailedException(String message) {
        super(message);
    }

}
