package io.cockroachdb.bootcamp.transactions;

import java.math.BigDecimal;
import java.util.UUID;

public interface InventoryService {
    boolean verifyProductInventory(UUID id, BigDecimal price, int quantity);
}
