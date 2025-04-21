package com.taylor.common.access.aspect;

import cn.hutool.core.text.CharSequenceUtil;
import com.taylor.common.access.annotation.RateLimit;
import com.taylor.common.access.service.RateLimiter;
import com.taylor.common.web.exception.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 控制切面
 *
 * @author loveCamille
 * @date 2025-04-19 00:02:27
 */
@Slf4j
@Aspect
public class RateLimitAspect {

    private final RateLimiter rateLimiter;

    private final ExpressionParser parser = new SpelExpressionParser();

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public RateLimitAspect(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    /**
     * 环绕增强处理
     */
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 解析SpEL表达式生成限流key
        String key = parseKey(rateLimit, joinPoint);

        // 执行限流检查
        boolean acquired = rateLimiter.tryAcquire(
                key,
                rateLimit.limit(),
                rateLimit.time(),
                rateLimit.timeUnit()
        );

        if (!acquired) {
            String message = formatMessage(rateLimit, key);
            log.warn("Rate limit triggered: {}", message);
            throw new RateLimitException(message);
        }

        return joinPoint.proceed();
    }

    /**
     * 解析SpEL表达式生成实际key
     * @author loveCamille
     * @param rateLimit 注解信息
     * @param joinPoint 切点
     * @return {@link String} 实际key
     */
    private String parseKey(RateLimit rateLimit, ProceedingJoinPoint joinPoint) {
        String keyTemplate = rateLimit.key();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        String prefix = CharSequenceUtil.isEmpty(rateLimit.prefix()) ?
                method.getClass().getName() + "#" + method.getName() : rateLimit.prefix();
        // 创建SpEL上下文
        EvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        // 解析表达式
        Expression expression = parser.parseExpression(keyTemplate);
        return prefix + ":" + expression.getValue(context, String.class);
    }

    /**
     * 格式化错误信息
     * @author loveCamille
     * @param rateLimit 限流信息
     * @param key 限流键
     * @return {@link String} 格式化后的错误
     */
    private String formatMessage(RateLimit rateLimit, String key) {
        return rateLimit.message()
                .replace("{key}", key)
                .replace("{limit}", String.valueOf(rateLimit.limit()))
                .replace("{time}", String.valueOf(rateLimit.time()))
                .replace("{unit}", translateTimeUnit(rateLimit.timeUnit()));
    }

    /**
     * 转换时间单位为中文
     * @author loveCamille
     * @param unit 时间单位
     * @return {@link String} 时间单位中文
     */
    private String translateTimeUnit(TimeUnit unit) {
        switch (unit) {
            case SECONDS: return "秒";
            case MINUTES: return "分钟";
            case HOURS: return "小时";
            case DAYS: return "天";
            default: return unit.toString().toLowerCase();
        }
    }

}
