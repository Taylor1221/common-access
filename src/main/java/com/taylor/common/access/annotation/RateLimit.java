package com.taylor.common.access.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

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
     * <p>每个请求在设定的时间内允许最大调用次数（即限流次数）。</p>
     * <p>例如：设置为5，表示该接口在设定时间内最多允许被调用5次。</p>
     */
    int limit() default 1;

    /**
     * <p>请求的时间窗口，单位为秒，表示在该时间范围内进行限流计数。</p>
     * <p>比如：设置为60，表示该接口最多可以在60秒内被调用 <code>maxCount</code> 次。</p>
     */
    int timeout() default 60;

    /**
     * <p>提示信息，提供给前端用于展示限制原因。</p>
     * <p>例如：登录失败次数过多，验证码请求频繁等。</p>
     * <p>该消息将作为错误提示返回给前端，帮助用户理解被限制的原因。</p>
     */
    String message() default "操作过于频繁，请稍后再试";

}
