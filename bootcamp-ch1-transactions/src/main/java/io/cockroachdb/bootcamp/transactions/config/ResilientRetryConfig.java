package io.cockroachdb.bootcamp.transactions.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Role;
import org.springframework.resilience.annotation.EnableResilientMethods;

import io.cockroachdb.bootcamp.aspect.AdvisorOrder;
import io.cockroachdb.bootcamp.aspect.TransientExceptionClassifier;

@Configuration
@EnableResilientMethods(proxyTargetClass = true, order = AdvisorOrder.TRANSACTION_BEFORE_ADVISOR)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Profile("!aspect-retry")
public class ResilientRetryConfig {
    @Bean
    public TransientExceptionClassifier exceptionClassifier() {
        return new TransientExceptionClassifier();
    }
}
