package io.cockroachdb.bootcamp.batching;

import java.util.Collection;
import java.util.function.Consumer;

import io.cockroachdb.bootcamp.model.PurchaseOrder;

public interface OrderService {
    void placeOrder(PurchaseOrder order);

    void placeOrders(Collection<PurchaseOrder> orders, int batchSize, Consumer<Integer> consumer);

    void placeOrderChunk(Collection<PurchaseOrder> chunk);
}
