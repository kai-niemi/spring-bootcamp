package io.cockroachdb.bootcamp.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Role;

import io.cockroachdb.bootcamp.aspect.DefaultRetryHandler;
import io.cockroachdb.bootcamp.aspect.RetryHandler;
import io.cockroachdb.bootcamp.aspect.TransactionRetryAspect;

/**
 * Custom AspectJ alternative to spring-retry.
 */
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Profile("!spring-retry")
public class AspectRetryConfig {
    @Bean
    public TransactionRetryAspect transactionRetryAspect() {
        return new TransactionRetryAspect(retryHandler());
    }

    @Bean
    public RetryHandler retryHandler() {
        return new DefaultRetryHandler();
    }
}
