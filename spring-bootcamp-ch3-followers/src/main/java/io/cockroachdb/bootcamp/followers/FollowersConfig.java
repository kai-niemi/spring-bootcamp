package io.cockroachdb.bootcamp.followers;

import javax.sql.DataSource;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Role;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class FollowersConfig {
    @Bean
    public FollowerReadAspect followerReadAspect(DataSource dataSource) {
        return new FollowerReadAspect(dataSource);
    }
}
