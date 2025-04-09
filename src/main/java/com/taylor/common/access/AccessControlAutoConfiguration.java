package com.taylor.common.access;

import com.taylor.common.access.aspect.AccessControlAspect;
import com.taylor.common.access.service.AccessControlService;
import com.taylor.common.access.service.impl.InMemoryAccessControlService;
import com.taylor.common.access.service.impl.RedisAccessControlService;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 访问控制自动配置
 *
 * @author loveCamille
 * @date 2025-04-09 11:35:58
 */
@Configuration(proxyBeanMethods = false)
public class AccessControlAutoConfiguration {

    @Bean
    public AccessControlAspect accessControlAspect() {
        return new AccessControlAspect();
    }

    @Bean
    @ConditionalOnMissingBean(AccessControlService.class)
    public AccessControlService inMemoryAccessControlService() {
        return new InMemoryAccessControlService();
    }

    @Bean
    @ConditionalOnClass({RedissonClient.class})
    public AccessControlService redisAccessControlService() {
        return new RedisAccessControlService();
    }

}
