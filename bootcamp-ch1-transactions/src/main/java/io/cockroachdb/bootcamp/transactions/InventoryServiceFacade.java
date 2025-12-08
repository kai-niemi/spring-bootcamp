package io.cockroachdb.bootcamp.transactions;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cockroachdb.bootcamp.annotation.ServiceFacade;

@ServiceFacade
public class InventoryServiceFacade implements InventoryService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean verifyProductInventory(UUID id, BigDecimal price, int quantity) {
        // Were slow, but always successful
        try {
            long delay = ThreadLocalRandom.current().nextLong(1000, 5000);
            logger.info("Validating product id=%s, price=%s, qty=%d, delay=%d"
                    .formatted(id, price, quantity, delay));
            TimeUnit.MILLISECONDS.sleep(delay);
            logger.info("Validated product id=%s, price=%s, qty=%d - all good ٩(^‿^)۶"
                    .formatted(id, price, quantity));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }
}
