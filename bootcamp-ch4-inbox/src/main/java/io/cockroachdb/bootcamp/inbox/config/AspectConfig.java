package io.cockroachdb.bootcamp.inbox.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Role;

import io.cockroachdb.bootcamp.inbox.aspect.IdempotencyAspect;
import io.cockroachdb.bootcamp.repository.IdempotencyTokenRepository;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AspectConfig {
    @Bean
    public IdempotencyAspect idempotencyAspect(@Autowired IdempotencyTokenRepository repository) {
        return new IdempotencyAspect(repository);
    }
}
