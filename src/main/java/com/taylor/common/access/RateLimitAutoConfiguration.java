package com.taylor.common.access;

import com.taylor.common.access.aspect.RateLimitAspect;
import com.taylor.common.access.service.RateLimiter;
import com.taylor.common.access.service.impl.InMemoryRateLimiter;
import com.taylor.common.access.service.impl.RedissonRateLimiter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 限流自动配置
 *
 * @author loveCamille
 * @date 2025-04-21 16:35:33
 */
@Configuration(proxyBeanMethods = false)
public class RateLimitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RateLimiter.class)
    public RateLimiter rateLimiter() {
        return new InMemoryRateLimiter();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Redisson.class)
    public static class RedissonRateLimiterConfiguration {

        @Bean
        public RateLimiter rateLimiter(RedissonClient redissonClient) {
            return new RedissonRateLimiter(redissonClient);
        }

    }

    @Bean
    public RateLimitAspect rateLimitAspect(RateLimiter rateLimiter) {
        return new RateLimitAspect(rateLimiter);
    }

}
