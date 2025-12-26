package io.cockroachdb.bootcamp.inbox.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import tools.jackson.databind.SerializationFeature;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.enable(SerializationFeature.INDENT_OUTPUT);
    }
}
