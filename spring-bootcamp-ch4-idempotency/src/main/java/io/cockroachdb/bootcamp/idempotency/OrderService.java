package io.cockroachdb.bootcamp.idempotency;

import java.util.UUID;

import io.cockroachdb.bootcamp.model.PurchaseOrder;

public interface OrderService {
    PurchaseOrder placeOrder(UUID idempotencyKey, PurchaseOrder order);
}
