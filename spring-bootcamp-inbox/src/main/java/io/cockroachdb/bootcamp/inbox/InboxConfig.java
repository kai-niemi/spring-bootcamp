package io.cockroachdb.bootcamp.inbox;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Role;
import org.springframework.kafka.annotation.EnableKafka;

import tools.jackson.databind.SerializationFeature;

@Configuration
@EnableKafka
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class InboxConfig {
    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.enable(SerializationFeature.INDENT_OUTPUT);
    }
}
