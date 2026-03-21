package io.cockroachdb.bootcamp.followers;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.function.Consumer;

import io.cockroachdb.bootcamp.model.PurchaseOrder;

public interface OrderService {
    BigDecimal sumOrderTotals();

    BigDecimal sumOrderTotalsHistoricalQuery();

    BigDecimal sumOrderTotalsHistoricalNativeQuery();

    void placeOrders(Collection<PurchaseOrder> orders, int batchSize, Consumer<Integer> consumer);

    void placeOrderChunk(Collection<PurchaseOrder> chunk);
}
