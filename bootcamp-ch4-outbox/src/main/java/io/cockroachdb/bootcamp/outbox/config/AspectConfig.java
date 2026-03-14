package io.cockroachdb.bootcamp.outbox.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Role;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AspectConfig {
}
