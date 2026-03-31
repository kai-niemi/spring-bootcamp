package io.cockroachdb.bootcamp.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

@Component
public class ChangeFeedListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private CacheManager cacheManager;

    /**
     * Change feed listener for 'purchase_order' table range feeds. Uses unique groupId's to
     * make each consumer work independently (JMS topic consumer equivalent) as opposed to
     * jointly in order to invalidate all local caches on state mutations.
     *
     * @param event the change event with envelope
     */
    @KafkaListener(
            id = "cache-demo",
            topics = "purchase_order",
            groupId = "#{T(java.util.UUID).randomUUID().toString()}",
            properties = {"spring.json.value.default.type=io.cockroachdb.bootcamp.cache.ChangeEvent"})
    public void onPurchaseOrderEvent(ChangeEvent event) {
        logger.debug("Received change feed event: {}",
                jsonMapper.writer()
                        .withDefaultPrettyPrinter()
                        .writeValueAsString(event));

        // Invalidate local cache on all mutations
        Object orderId = event.getAfter() != null
                ? event.getAfter().getOrDefault("id", null) : null;
        if (orderId != null) {
            Cache cache = cacheManager.getCache("orders");
            if (cache != null) {
                cache.evictIfPresent(orderId);
                logger.info("Evicted order id: %s".formatted(orderId));
            }
        }
    }
}
