package io.cockroachdb.bootcamp.locking.shedlock;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.provider.sql.DatabaseProduct;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

import static net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider.Configuration.builder;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
@Profile(value = "shedlock")
public class ShedLockConfig {
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .withDatabaseProduct(DatabaseProduct.COCKROACH_DB)
                .usingDbTime()
                .build()
        );
    }
}
