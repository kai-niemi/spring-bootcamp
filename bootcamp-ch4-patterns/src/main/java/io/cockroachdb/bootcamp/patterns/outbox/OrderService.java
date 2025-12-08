package io.cockroachdb.bootcamp.patterns.outbox;

import io.cockroachdb.bootcamp.model.PurchaseOrder;

public interface OrderService {
    PurchaseOrder placeOrder(PurchaseOrder order);
}
