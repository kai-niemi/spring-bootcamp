package io.cockroachdb.bootcamp.transactions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;

@Profile({"default"})
public class ReadWriteTest extends AbstractOrderPlacementTest {
    @Autowired
    @Qualifier("readWriteOrderService")
    private OrderService orderService;

    @Override
    protected OrderService orderService() {
        return orderService;
    }
}

