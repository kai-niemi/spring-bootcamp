package io.cockroachdb.bootcamp.patterns.inbox;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.json.JsonMapper;

import io.cockroachdb.bootcamp.model.PurchaseOrder;

@Repository
public class InboxJdbcRepository implements InboxRepository<PurchaseOrder> {
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
    public void writeAggregate(PurchaseOrder purchaseOrder, String aggregateType) {
        String json = jsonMapper.writer().writeValueAsString(purchaseOrder);

        logger.debug("Write inbox event: {}", json);

        jdbcTemplate.update(
                "INSERT INTO inbox (aggregate_type,payload) VALUES (?, ?) RETURNING NOTHING",
                ps -> {
                    ps.setString(1, aggregateType);
                    ps.setObject(2, json);
                });
    }
}
