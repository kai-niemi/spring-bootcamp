package io.cockroachdb.bootcamp.transactions.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Role;

import io.cockroachdb.bootcamp.transactions.aspect.DefaultRetryHandler;
import io.cockroachdb.bootcamp.transactions.aspect.RetryHandler;
import io.cockroachdb.bootcamp.transactions.aspect.TransactionRetryAspect;

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Profile("aspect-retry")
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
