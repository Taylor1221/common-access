package com.taylor.common.access.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BanLimit {

    /**
     * <p>访问控制的键，通常是方法的参数，作为访问控制的唯一标识。</p>
     * <p>比如：手机号、用户名等。可以通过SpEL表达式动态传递。</p>
     */
    String key();

    /**
     * <p>前缀，用于标识该条访问控制规则的类型，如验证码、登录失败等。</p>
     * <p>这样可以组合 <code>key</code> 构成唯一的访问控制标识。</p>
     * <p>例如：`sms:123456789`</p>
     */
    String prefix() default "";

    /**
     * 最大失败次数
     */
    int maxFailAttempts() default 5;

    /**
     * 过期时间
     */
    int timeout() default 60;

    /**
     * 封禁时长（单位秒）
     */
    int blockDuration() default 900; // 默认15分钟

    String message() default "失败次数过多，";

}
