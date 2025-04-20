package com.taylor.common.access.annotation;

import com.taylor.common.access.constant.Group;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Block {

    String prefix() default "";

    String key();

    int maxAttempts() default 5;

    long time() default 60;

    TimeUnit timeUnit() default TimeUnit.MINUTES;

    Group group();

    String message() default "操作被限制，请稍后重试";

}
