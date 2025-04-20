package com.taylor.common.access.aspect;

import cn.hutool.core.date.DateUtil;
import com.taylor.common.access.annotation.BanLimit;
import com.taylor.common.access.annotation.RateLimit;
import com.taylor.common.access.builder.KeyBuilder;
import com.taylor.common.access.constant.AccessConstant;
import com.taylor.common.access.domain.Frozen;
import com.taylor.common.access.exception.FailedException;
import com.taylor.common.access.service.AccessControlService;
import com.taylor.common.web.domain.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * 请求频率的限流
 *
 * @author loveCamille
 * @date 2025-04-09 09:35:02
 */
@Aspect
public class AccessControlAspect {

    @Autowired
    private AccessControlService accessControlService;

    private final ExpressionParser parser = new SpelExpressionParser();

    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();


    @Around("@annotation(banLimit)")
    public Object handleBanLimit(ProceedingJoinPoint joinPoint, BanLimit banLimit) throws Throwable {
        // 获取限流的唯一键
        String key = buildBannedKey(joinPoint, banLimit);
        boolean isBanned = accessControlService.isBanned(key);
        // 超过限制，返回限流响应
        if (isBanned) {
            Frozen banned = accessControlService.getBanned(key);
            return Result.fail(banLimit.message() + "请于 "
                    + DateUtil.formatDateTime(banned.getExpireAfter()) + " 后重试");
        }
        try {
            Object result = joinPoint.proceed();
            accessControlService.removeBanned(key);
            return result;
        } catch (FailedException e) {
            accessControlService.incrementFailAttempts(key, banLimit.timeout(),
                    banLimit.maxFailAttempts(), banLimit.blockDuration());
            return Result.fail(e.getMessage());
        }
    }

    private String getSpElExpressionVal(ProceedingJoinPoint joinPoint, String expression) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String[] paramNames = nameDiscoverer.getParameterNames(method);
        String keyValue = "";
        if (null != paramNames && paramNames.length > 0) {
            Object[] args = joinPoint.getArgs();
            EvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            keyValue = parser.parseExpression(expression).getValue(context, String.class);
        }
        return keyValue;
    }

    /**
     * 构建限流key
     * 由 RATE_LIMIT_PREFIX + 注释上的prefix（没有则是方法名）+ 用户标识
     * @author loveCamille
     * @param joinPoint 切点
     * @param rateLimit 方法上的注解
     * @return {@link String} 对应的key
    */
    private String buildRateLimitKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String prefix = rateLimit.prefix();
        if (prefix.isEmpty()) {
            prefix = method.getName();
        }
        String keyValue = getSpElExpressionVal(joinPoint, rateLimit.key());
        return new KeyBuilder().add(AccessConstant.PREFIX).add(AccessConstant.RATE_LIMIT_PREFIX)
                .add(prefix).add(keyValue).build();
    }

    /**
     * 构建封禁key
     * 由 BANNED_PREFIX + 注释上的prefix（没有则是方法名）+ 用户标识
     * @author loveCamille
     * @param joinPoint 切点
     * @param banLimit 方法上的注解
     * @return {@link String} 对应的key
     */
    private String buildBannedKey(ProceedingJoinPoint joinPoint, BanLimit banLimit) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String prefix = banLimit.prefix();
        if (prefix.isEmpty()) {
            prefix = method.getName();
        }
        String keyValue = getSpElExpressionVal(joinPoint, banLimit.key());
        return new KeyBuilder().add(AccessConstant.PREFIX).add(AccessConstant.BANNED_PREFIX)
                .add(prefix).add(keyValue).build();
    }

}
