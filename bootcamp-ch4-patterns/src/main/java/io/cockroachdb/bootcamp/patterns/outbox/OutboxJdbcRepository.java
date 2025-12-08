package io.cockroachdb.bootcamp.patterns.outbox;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class OutboxJdbcRepository implements OutboxRepository {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JsonMapper jsonMapper;

    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void writeEvent(Object event, String aggregateType) {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(),
                "Expected existing transaction - check advisor @Order");

        String json = jsonMapper.writer().writeValueAsString(event);

        logger.info("Writing outbox event: {}", json);

        jdbcTemplate.update(
                "INSERT INTO outbox (aggregate_type,payload) VALUES (?,?)",
                ps -> {
                    ps.setString(1, aggregateType);
                    ps.setObject(2, json);
                });
    }
}
