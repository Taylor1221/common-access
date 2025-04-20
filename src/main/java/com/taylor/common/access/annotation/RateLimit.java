package com.taylor.common.access.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解
 * <p>使用示例：
 * <pre>{@code
 * @RateLimit(
 *     key = "'sms:' + #mobile",
 *     limit = 1,
 *     time = 1,
 *     timeUnit = TimeUnit.MINUTES,
 *     message = "手机号{key}操作过于频繁，请{time}{unit}后再试"
 * )
 * public void sendSms(String mobile) {...}
 * }</pre>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流键（支持SpEL表达式）
     * <p>示例：</p>
     * <ul>
     *   <li>'sms:' + #mobile  → 短信接口按手机号限流</li>
     *   <li>T(java.util.UUID).randomUUID()  → 使用UUID</li>
     * </ul>
     */
    String key();

    /**
     * <p>前缀，用于标识该条访问控制规则的类型，如验证码、登录失败等。</p>
     * <p>这样可以组合 <code>key</code> 构成唯一的访问控制标识。</p>
     * <p>例如：`sms:123456789`</p>
     */
    String prefix() default "";

    /**
     * 时间窗口内允许的最大请求次数
     * <p>默认：1次（适合短信验证码场景）</p>
     */
    int limit() default 1;

    /**
     * 时间窗口数值 默认1分钟
     */
    long time() default 60;

    /**
     * 时间单位
     * <p>支持从毫秒到天的多种时间单位</p>
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

    /**
     * 限流触发时的提示信息（支持简单表达式）
     * <p>支持占位符：</p>
     * <ul>
     *   <li>{key} → 替换为实际的限流键</li>
     *   <li>{limit} → 替换为限制次数</li>
     *   <li>{time} → 时间数值</li>
     *   <li>{unit} → 时间单位</li>
     * </ul>
     * <p>示例：</p>
     * <ul>
     *   <li>"操作过于频繁，请{time}{unit}后再试" → "操作过于频繁，请1分钟后再试"</li>
     *   <li>"资源{key}已达到访问上限" → "资源sms:13800138000已达到访问上限"</li>
     * </ul>
     */
    String message() default "操作过于频繁，请稍后再试";

}
