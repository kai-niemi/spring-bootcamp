package io.cockroachdb.bootcamp.patterns;

import io.cockroachdb.bootcamp.model.PurchaseOrder;

public interface OrderService {
    PurchaseOrder placeOrder(PurchaseOrder order);
}
