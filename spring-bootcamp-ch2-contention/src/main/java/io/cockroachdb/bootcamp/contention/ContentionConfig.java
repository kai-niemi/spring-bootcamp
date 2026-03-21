package io.cockroachdb.bootcamp.contention;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Role;
import org.springframework.resilience.annotation.EnableResilientMethods;

import io.cockroachdb.bootcamp.aspect.AdvisorOrder;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableResilientMethods(proxyTargetClass = true, order = AdvisorOrder.TRANSACTION_BEFORE_ADVISOR)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ContentionConfig {
}
