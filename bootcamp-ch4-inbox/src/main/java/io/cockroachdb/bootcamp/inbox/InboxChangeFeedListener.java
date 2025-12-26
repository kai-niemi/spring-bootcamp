package io.cockroachdb.bootcamp.inbox;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import io.cockroachdb.bootcamp.model.PurchaseOrder;
import io.cockroachdb.bootcamp.inbox.event.PurchaseOrderEvent;

@Component
public class InboxChangeFeedListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private OrderService orderService;

    @KafkaListener(id = "inbox-demo", topics = "orders-inbox", groupId = "bootcamp",
            properties = {"spring.json.value.default.type=io.cockroachdb.bootcamp.inbox.event.PurchaseOrderEvent"})
    public void onPurchaseOrderEvent(PurchaseOrderEvent event) {
        Objects.requireNonNull(event.getAggregateId(), "aggregateId is null");

        logger.debug("Received inbox event with idempotency key '%s': %s"
                .formatted(event.getAggregateId(),
                        jsonMapper.writer().writeValueAsString(event)
                ));

        PurchaseOrder transientEntity = PurchaseOrder.builder()
                .withCustomer(event.getPayload().getCustomer())
                .andOrderItems(event.getPayload().getOrderItems())
                .build();

        orderService.placeOrder(event.getAggregateId(), transientEntity);
    }
}
