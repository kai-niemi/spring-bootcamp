package io.cockroachdb.bootcamp.patterns.inbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import tools.jackson.databind.json.JsonMapper;

import io.cockroachdb.bootcamp.annotation.ServiceFacade;
import io.cockroachdb.bootcamp.patterns.OrderService;
import io.cockroachdb.bootcamp.patterns.PurchaseOrderEvent;

@ServiceFacade
public class InboxChangeFeedListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private OrderService orderService;

    @KafkaListener(id = "inbox-demo", topics = "orders-inbox", groupId = "bootcamp",
            properties = {"spring.json.value.default.type=io.cockroachdb.bootcamp.patterns.PurchaseOrderEvent"})
    public void onPurchaseOrderEvent(PurchaseOrderEvent event) {
        logger.debug("Received inbox event: {}",
                jsonMapper.writer().writeValueAsString(event));
        orderService.placeOrder(event.getPayload());
    }
}
