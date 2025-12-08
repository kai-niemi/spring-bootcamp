package io.cockroachdb.bootcamp.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Role;

import io.cockroachdb.bootcamp.aspect.TransactionAttributeAspect;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AspectConfig {
    @Bean
    public TransactionAttributeAspect transactionDecoratorAspect(DataSource dataSource) {
        return new TransactionAttributeAspect(dataSource);
    }
}
