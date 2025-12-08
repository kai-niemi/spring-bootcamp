package io.cockroachdb.bootcamp.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.resilience.annotation.EnableResilientMethods;

import io.cockroachdb.bootcamp.aspect.AdvisorOrder;
import io.cockroachdb.bootcamp.aspect.TransientExceptionClassifier;

@Configuration
@EnableResilientMethods(proxyTargetClass = true, order = AdvisorOrder.TRANSACTION_RETRY_ADVISOR)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class SpringRetryConfig {
    @Bean
    public TransientExceptionClassifier exceptionClassifier() {
        return new TransientExceptionClassifier();
    }
}
