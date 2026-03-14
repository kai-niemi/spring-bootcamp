package io.cockroachdb.bootcamp.locking;

import java.util.UUID;

import io.cockroachdb.bootcamp.model.PurchaseOrder;

/**
 * @author Kai Niemi
 */
public interface OrderService {
    PurchaseOrder placeOrder(UUID idempotencyKey, PurchaseOrder order);
}
