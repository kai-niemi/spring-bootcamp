package io.cockroachdb.bootcamp.cdc.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import io.cockroachdb.bootcamp.cdc.event.PurchaseOrderEvent;

@Component
public class OutboxChangeFeedListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JsonMapper jsonMapper;

    @KafkaListener(id = "outbox-demo", topics = "orders-outbox", groupId = "bootcamp",
            properties = {"spring.json.value.default.type=io.cockroachdb.bootcamp.patterns.event.PurchaseOrderEvent"})
    public void onPurchaseOrderEvent(PurchaseOrderEvent event) {
        logger.debug("Received outbox event: {}",
                jsonMapper.writer().writeValueAsString(event));
    }
}
