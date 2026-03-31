package io.cockroachdb.bootcamp.locking;

import io.cockroachdb.bootcamp.model.PurchaseOrder;

public interface OrderService {
    /**
     * Place a purchase order.
     *
     * @param order the new order in detached state
     * @return a copy of the order in detached state
     */
    PurchaseOrder placeOrder(PurchaseOrder order);
}
