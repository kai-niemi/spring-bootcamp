package io.cockroachdb.bootcamp.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.cockroachdb.bootcamp.aspect.AdvisorOrder;

@Configuration
@EnableJpaRepositories(basePackages = "io.cockroachdb.bootcamp", enableDefaultTransactions = false)
@EnableTransactionManagement(proxyTargetClass = true, order = AdvisorOrder.TRANSACTION_BOUNDARY_ADVISOR)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class TransactionConfig {
}
